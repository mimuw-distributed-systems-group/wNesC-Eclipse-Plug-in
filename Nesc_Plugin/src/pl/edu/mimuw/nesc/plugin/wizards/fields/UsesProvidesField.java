package pl.edu.mimuw.nesc.plugin.wizards.fields;

import pl.edu.mimuw.nesc.plugin.wizards.fields.UsesProvidesField.UsesProvides;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;


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
    private static final String CELL_NO_INSTANCE_NAME = "<default>";


    /**
     * Columns specifications for this field.
     */
    private static final ColumnSpecification[] FIELD_COLUMN_SPEC = {
        new ColumnSpecification("Type", 60, "Type of the element"),
        new ColumnSpecification("Interface", 120, "Interface that is provided or used"),
        new ColumnSpecification("Name", 100, "Name that will be used to refer to "
                + "this instance of the interface")
    };

    /**
     * Shell used for showing dialogs. Not null.
     */
    private final Shell shell;

    /**
     * Initializes the field - creates all necessary controls.
     *
     * @param parent Composite that will contain this fields controls.
     * @param layoutData Layout data object that affects the layout of the
     *                   composite created for this field.
     * @param shell Parent shell for showing dialogs.
     * @throws NullPointerException <code>parent</code> or <code>shell</code>
     *                              is null.
     */
    public UsesProvidesField(Composite parent, Object layoutData, Shell shell) {
        super(parent, FIELD_NAME, layoutData, FIELD_COLUMN_SPEC);

        if (shell == null) {
            throw new NullPointerException("Shell cannot be null.");
        }

        this.shell = shell;
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
        result[2] =    !usesProvides.getInstanceName().isEmpty()
                    ?  usesProvides.getInstanceName()
                    :  CELL_NO_INSTANCE_NAME;

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
         * A name that allows referring to the instance of an interface instead
         * of the name of the interface itself. Never null. Empty value means no
         * instance name.
         */
        private String instanceName;

        /**
         * Initializes fields of the object.
         *
         * @throws NullPointerException One of the arguments is null.
         * @throws IllegalArgumentException Interface name is empty.
         */
        UsesProvides(Type type, String interfaceName, String instanceName) {
            setType(type);
            setInterfaceName(interfaceName);
            setInstanceName(instanceName);
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
         * Sets the instance name to the given value.
         *
         * @throws NullPointerException The argument is null.
         */
        void setInstanceName(String instanceName) {
            if (instanceName == null) {
                throw new NullPointerException("Instance name cannot be null.");
            }

            this.instanceName = instanceName;
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
         * @return Instance name that this object carries. Never null.
         */
        public String getInstanceName() {
            return instanceName;
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
    }
}
