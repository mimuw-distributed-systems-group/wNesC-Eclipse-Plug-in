package pl.edu.mimuw.nesc.plugin.wizards.fields;

import pl.edu.mimuw.nesc.plugin.wizards.fields.UsesProvidesField.UsesProvides;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Field that allows entering uses/provides entries.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public final class UsesProvidesField extends TableField<UsesProvides> {
    /**
     * Name of this field.
     */
    private static final String FIELD_NAME = "Uses/provides";

    /**
     * Various text used for this field.
     */
    private static final String TITLE_ADD = "New uses/provides item";
    private static final String TITLE_EDIT = "Edit uses/provides item";
    private static final String CELL_PROVIDES = "provides";
    private static final String CELL_USES = "uses";
    private static final String CELL_NO_INSTANCE_SPEC = "<default>";


    /**
     * Columns specifications for this field.
     */
    private static final ColumnSpecification[] FIELD_COLUMN_SPEC = {
        new ColumnSpecification("Type", 60, "Type of the element"),
        new ColumnSpecification("Interface", 120, "Interface that is provided or used"),
        new ColumnSpecification("Instance", 100, "Specification of the interface instance. "
                + "It defines the instance name and parameters.")
    };

    /**
     * Shell used for showing dialogs. Not null.
     */
    private final Shell shell;

    /**
     * Get the builder for an object of this class.
     *
     * @return Newly created builder for objects of this class.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Initializes the field - creates all necessary controls.
     *
     * @param builder Builder for this class.
     */
    private UsesProvidesField(Builder builder) {
        super(builder);
        this.shell = builder.parentShell;
    }

    @Override
    protected void addItemOperation() {
        final String[] deniedNames = prepareDeniedInstanceNames(null);
        final UsesProvidesDialog dialog = new UsesProvidesDialog(shell, TITLE_ADD,
                deniedNames, null);

        if (dialog.open() == Dialog.OK) {
            final TableItem item = newItem(dialog.getUsesProvides());
            item.setText(prepareTableRow(dialog.getUsesProvides()));
        }
    }

    @Override
    protected void editItemOperation(TableItem item, UsesProvides usesProvides) {
        final String[] deniedNames = prepareDeniedInstanceNames(usesProvides);
        final UsesProvidesDialog dialog = new UsesProvidesDialog(shell, TITLE_EDIT,
                deniedNames, usesProvides);

        if (dialog.open() == Dialog.OK) {
            dialog.insertValues(usesProvides);
            item.setText(prepareTableRow(usesProvides));
        }
    }

    @Override
    public String getErrorStatus() {
        return null;
    }

    /**
     * Array with forbidden instance names for use in the uses/provides dialog.
     *
     * @param allowedName UsesProvides object whose name will not be added to
     *                    the returned array (however, it will be added if other
     *                    entries with the same name exist).
     * @return Newly created array with denied instance names.
     */
    private String[] prepareDeniedInstanceNames(UsesProvides allowedName) {
        final List<String> deniedNames = new ArrayList<>();
        final List<UsesProvides> values = getValue();

        for (UsesProvides value : values) {
            if (value != allowedName) {
                deniedNames.add(value.getEffectiveInstanceName());
            }
        }

        return deniedNames.toArray(new String[deniedNames.size()]);
    }

    /**
     * @param usesProvides Object to be represented by a table row.
     * @return Values in subsequent columns of the table that correspond
     *         to the given object.
     */
    private String[] prepareTableRow(UsesProvides usesProvides) {
        final String[] result = new String[3];

        // Type of the entry
        usesProvides.getType().accept(new UsesProvides.Type.Visitor(){
            @Override
            public void visit(UsesProvides.Uses marker) {
                result[0] = CELL_USES;
            }
            @Override
            public void visit(UsesProvides.Provides marker) {
                result[0] = CELL_PROVIDES;
            }
        });

        // Other values
        result[1] = usesProvides.getInterfaceName();
        result[2] =    !usesProvides.getInstanceSpec().isEmpty()
                    ?  usesProvides.getInstanceSpec()
                    :  CELL_NO_INSTANCE_SPEC;

        return result;
    }

    /**
     * Class hierarchy that represent a uses/provides item. Part of it follows
     * the Visitor design pattern.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    public static class UsesProvides {
        /**
         * Type of the entry: uses or provides. Never null.
         */
        private Type type;

        /**
         * Name of the interface that is provides or used. Can contain angle
         * braces with the type parameter. Never null.
         */
        private String interfaceName;

        /**
         * Specification of the instance. If consists of the instance name
         * and/or instance parameters. Never null. Empty value means no instance
         * name and no instance parameters.
         */
        private String instanceSpec;

        /**
         * Name of the instance extracted from the instance specification.
         * Never null. Empty values means no instance name.
         */
        private String instanceName;

        /**
         * Parameters of the instance extracted from the instance specification.
         * Never null. Empty value means no instance parameters.
         */
        private String instanceParameters;

        /**
         * Initializes fields of the object.
         *
         * @throws NullPointerException One of the arguments is null.
         * @throws IllegalArgumentException Interface name is empty.
         */
        UsesProvides(Type type, String interfaceName, String instanceSpec) {
            setType(type);
            setInterfaceName(interfaceName);
            setInstanceSpec(instanceSpec);
        }

        /**
         * Sets the interface name to the given value.
         *
         * @throws NullPointerException The argument is null.
         * @throws IllegalArgumentException The argument is an empty string.
         */
        void setInterfaceName(String interfaceName) {
           if (interfaceName == null) {
               throw new NullPointerException("The interface name cannot be null.");
           } else if (interfaceName.isEmpty()) {
               throw new IllegalArgumentException("Interface name cannot be empty.");
           }

           this.interfaceName = interfaceName;
        }

        /**
         * Sets the instance specification to the given value.
         *
         * @throws NullPointerException The argument is null.
         * @throws IllegalArgumentException The argument is invalid.
         */
        void setInstanceSpec(String instanceSpec) {
            // Parse the specification
            final InstanceSpecificationParser parser = new InstanceSpecificationParser(instanceSpec);
            if (!parser.correct()) {
                throw new IllegalArgumentException("Instance specification is invalid.");
            }

            // The specification is correct so set it
            this.instanceSpec = parser.getInstanceSpec();
            this.instanceName = parser.getInstanceName();
            this.instanceParameters = parser.getInstanceParams();
        }

        /**
         * Sets the type of the entry.
         *
         * @throws NullPointerException The argument is null.
         */
        void setType(Type type) {
            if (type == null) {
                throw new NullPointerException("Uses/provides type cannot be null.");
            }

            this.type = type;
        }

        /**
         * @return Type of this entry. Never null.
         */
        public Type getType() {
            return type;
        }

        /**
         * @return Interface name that this object carries. Never null or empty
         *         string.
         */
        public String getInterfaceName() {
            return interfaceName;
        }

        /**
         * @return Instance specification that this object carries. Never null.
         */
        public String getInstanceSpec() {
            return instanceSpec;
        }

        /**
         * @return Instance name that this object carries. Never null.
         */
        public String getInstanceName() {
            return instanceName;
        }

        /**
         * @return Instance parameters that this object carries. Never null.
         */
        public String getInstanceParameters() {
            return instanceParameters;
        }

        /**
         * @return Name that will be used by nesC as an instance name for this
         *         uses/provides entry. Never null.
         */
        public String getEffectiveInstanceName() {
            if (!instanceName.isEmpty()) {
                return instanceName;
            }

            final int leftAnglePos = interfaceName.indexOf('<');
            if (leftAnglePos == -1) {
                return interfaceName;
            } else {
                return interfaceName.substring(0, leftAnglePos).trim();
            }
        }

        /**
         * Class hierarchy that depicts the type of the uses/provides entry.
         * It follows the Visitor design pattern.
         *
         * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
         */
        public static abstract class Type {
            private Type() {}
            public abstract void accept(Visitor visitor);

            public interface Visitor {
                void visit(Uses marker);
                void visit(Provides marker);
            }
        }

        public static final class Uses extends Type {
            Uses() {}

            @Override
            public void accept(Visitor visitor) {
                visitor.visit(this);
            }
        }

        public static final class Provides extends Type {
            Provides() {}

            @Override
            public void accept(Visitor visitor) {
                visitor.visit(this);
            }
        }

        /**
         * Class that parses the instance specification text. It extracts the
         * instance name and instance parameters from it.
         *
         * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
         */
        final static class InstanceSpecificationParser {
            /**
             * Names of the groups used in regular expressions.
             */
            private static final String REGEXP_GROUP_NAME = "name";
            private static final String REGEXP_GROUP_PARAMS = "params";

            /**
             * Auxiliary regular expressions.
             */
            private static final String REGEXP_IDENTIFIER = "[a-zA-Z_]\\w*";
            private static final String REGEXP_PARAMETER = REGEXP_IDENTIFIER + "\\s+"
                    + REGEXP_IDENTIFIER;

            /**
             * Regular expression that defines the language of valid instance
             * specifications.
             */
            private static final String REGEXP_INSTANCE_SPEC = "^(?<" + REGEXP_GROUP_NAME + ">"
                    + REGEXP_IDENTIFIER + ")?(\\s*(?<" + REGEXP_GROUP_PARAMS + ">\\["
                    + REGEXP_PARAMETER + "([,]\\s*" + REGEXP_PARAMETER + ")*\\]))?$";

            /**
             * Instance specification that has been parsed and the instance
             * name and instance parameters from it. The name and parameters are
             * null if and only if the specification is invalid.
             */
            private final String instanceSpec;
            private final String instanceName;
            private final String instanceParams;

            /**
             * Parses the given instance specification and initializes the
             * object.
             *
             * @param instanceSpec Instance specification to parse.
             * @throws NullPointerException The given argument is null.
             */
            InstanceSpecificationParser(String instanceSpec) {
                if (instanceSpec == null) {
                    throw new NullPointerException("Instance specification cannot be null.");
                }

                this.instanceSpec = instanceSpec;

                // Parse the instance specification
                final Pattern pattern = Pattern.compile(REGEXP_INSTANCE_SPEC);
                final Matcher matcher = pattern.matcher(instanceSpec);
                if (!matcher.matches()) {
                    this.instanceName = null;
                    this.instanceParams = null;
                    return;
                }

                final String matchedName = matcher.group(REGEXP_GROUP_NAME),
                             matchedParams = matcher.group(REGEXP_GROUP_PARAMS);
                this.instanceName = matchedName != null ? matchedName : "";
                this.instanceParams = matchedParams != null ? matchedParams : "";
            }

            /**
             * @return True if and only if the instance specification has been
             *         correct.
             */
            boolean correct() {
                return instanceName != null && instanceParams != null;
            }

            /**
             * @return Instance specification that has been validated.
             *         Never null.
             */
            String getInstanceSpec() {
                return instanceSpec;
            }

            /**
             * @return Extracted instance name. Null if and only if the instance
             *         specification has been invalid.
             */
            String getInstanceName() {
                return instanceName;
            }

            /**
             * @return Extracted instance parameters. Null if and only if the
             *         specification has been invalid.
             */
            String getInstanceParams() {
                return instanceParams;
            }
        }
    }

    /**
     * Builder for this part of the fields hierarchy.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    public static final class Builder extends TableField.Builder<UsesProvides, UsesProvidesField> {
        /**
         * Constructor only for the class of object this builder creates.
         */
        private Builder() {
        }

        @Override
        protected void beforeBuild() {
            super.beforeBuild();
            setFieldName(FIELD_NAME);
            setColumnsSpec(FIELD_COLUMN_SPEC);
        }

        @Override
        protected void validate() {
            super.validate();
            checkNotNull(parentShell, "the parent shell cannot be null");
        }

        @Override
        protected UsesProvidesField create() {
            return new UsesProvidesField(this);
        }
    }
}
