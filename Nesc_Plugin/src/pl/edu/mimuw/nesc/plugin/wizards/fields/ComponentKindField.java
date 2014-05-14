package pl.edu.mimuw.nesc.plugin.wizards.fields;

import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import static org.eclipse.swt.SWT.*;

/**
 * Class that represents a field for choosing the kind of a nesC component.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public final class ComponentKindField extends AbstractField {
    /**
     * Name of this field.
     */
    private static final String FIELD_NAME = "Component kind";

    /**
     * Number of columns in the grid layout for this field.
     */
    private static final int FIELD_COLUMNS_COUNT = 2;

    /**
     * Labels for the controls of this field.
     */
    private static final String LABEL_MODULE = "module";
    private static final String LABEL_CONFIGURATION = "configuration";
    private static final String LABEL_BINARY_COMPONENT = "binary component";
    private static final String LABEL_GENERIC_MODULE = "generic module";
    private static final String LABEL_GENERIC_CONFIGURATION = "generic configuration";

    /**
     * Radio buttons that allow the user selecting the component type.
     */
    private final Button moduleRadio;
    private final Button configurationRadio;
    private final Button binaryComponentRadio;
    private final Button genericModuleRadio;
    private final Button genericConfigurationRadio;

    /**
     * Initializes the field and creates its controls.
     *
     * @param parent Composite that will contain controls of this field.
     * @param layoutData Layout data object that affects the layout of the
     *                   composite created for this field. May be null.
     * @throws NullPointerException <code>parent</code> is null.
     */
    public ComponentKindField(Composite parent, Object layoutData) {
        super(parent, FIELD_NAME, FIELD_COLUMNS_COUNT, layoutData);

        // Create radio buttons for this field
        final Composite radiosComposite = new Composite(getComposite(), NONE);
        radiosComposite.setLayout(new GridLayout(3, false));
        moduleRadio = new Button(radiosComposite, RADIO);
        moduleRadio.setText(LABEL_MODULE);
        configurationRadio = new Button(radiosComposite, RADIO);
        configurationRadio.setText(LABEL_CONFIGURATION);
        binaryComponentRadio = new Button(radiosComposite, RADIO);
        binaryComponentRadio.setText(LABEL_BINARY_COMPONENT);
        genericModuleRadio = new Button(radiosComposite, RADIO);
        genericModuleRadio.setText(LABEL_GENERIC_MODULE);
        genericConfigurationRadio = new Button(radiosComposite, RADIO);
        genericConfigurationRadio.setText(LABEL_GENERIC_CONFIGURATION);

        // Set the initial value
        moduleRadio.setSelection(true);
        configurationRadio.setSelection(false);
        binaryComponentRadio.setSelection(false);
        genericModuleRadio.setSelection(false);
        genericConfigurationRadio.setSelection(false);
    }

    @Override
    public void setFocus() {
        moduleRadio.forceFocus();
    }

    @Override
    public String getErrorStatus() {
        return null;
    }

    @Override
    public ComponentKind getValue() {
        if (moduleRadio.getSelection()) {
            return new Module();
        } else if (configurationRadio.getSelection()) {
            return new Configuration();
        } else if (binaryComponentRadio.getSelection()) {
            return new BinaryComponent();
        } else if (genericModuleRadio.getSelection()) {
            return new GenericModule();
        } else if (genericConfigurationRadio.getSelection()) {
            return new GenericConfiguration();
        } else {
            // this should never happen
            throw new RuntimeException("No component kind selected.");
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        moduleRadio.setEnabled(enabled);
        configurationRadio.setEnabled(enabled);
        binaryComponentRadio.setEnabled(enabled);
        genericModuleRadio.setEnabled(enabled);
        genericConfigurationRadio.setEnabled(enabled);
    }

    /**
     * The given listener will be notified when the value of this field
     * changes after call to this method.
     */
    public void addSelectionListener(SelectionListener listener) {
        moduleRadio.addSelectionListener(listener);
        configurationRadio.addSelectionListener(listener);
        binaryComponentRadio.addSelectionListener(listener);
        genericModuleRadio.addSelectionListener(listener);
        genericConfigurationRadio.addSelectionListener(listener);
    }

    /**
     * Class hierarchy for this field value instead of an enumeration type. It
     * is safer in use than an enumeration type with a switch statement - it
     * forces to provide code for all cases.
     * It follows the Visitor design pattern.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    public static abstract class ComponentKind {
        private ComponentKind() {}
        public abstract void accept(Visitor visitor);

        public interface Visitor {
            void visit(Module marker);
            void visit(Configuration marker);
            void visit(BinaryComponent marker);
            void visit(GenericModule marker);
            void visit(GenericConfiguration marker);
        }
    }

    public static final class Module extends ComponentKind {
        private Module() {}

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class Configuration extends ComponentKind {
        private Configuration() {}

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class BinaryComponent extends ComponentKind {
        private BinaryComponent() {}

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class GenericModule extends ComponentKind {
        private GenericModule() {}

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class GenericConfiguration extends ComponentKind {
        private GenericConfiguration() {}

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }
}
