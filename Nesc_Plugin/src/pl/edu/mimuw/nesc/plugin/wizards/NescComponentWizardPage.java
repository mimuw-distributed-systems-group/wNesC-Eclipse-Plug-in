package pl.edu.mimuw.nesc.plugin.wizards;

import pl.edu.mimuw.nesc.plugin.wizards.fields.*;
import pl.edu.mimuw.nesc.plugin.wizards.fields.ComponentKindField.*;
import pl.edu.mimuw.nesc.plugin.wizards.fields.GenericParametersField.GenericParameter;
import pl.edu.mimuw.nesc.plugin.wizards.fields.UsesProvidesField.UsesProvides;
import pl.edu.mimuw.nesc.plugin.wizards.fields.UsesProvidesField.UsesProvides.Provides;
import pl.edu.mimuw.nesc.plugin.wizards.fields.UsesProvidesField.UsesProvides.Uses;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import static org.eclipse.swt.SWT.*;

/**
 * Class that represents the only page of the component wizard.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public final class NescComponentWizardPage extends WizardPage {
    /**
     * Path to the plug-in resource with the icon for this wizard.
     */
    private static final String ICON_PATH = "resources/icons/componentWizardIcon.png";

    /**
     * Keywords for component generation.
     */
    private static final String KEYWORD_MODULE = "module";
    private static final String KEYWORD_CONFIGURATION = "configuration";
    private static final String KEYWORD_BINARY_COMPONENT = "component";
    private static final String KEYWORD_GENERIC_MODULE = "generic module";
    private static final String KEYWORD_GENERIC_CONFIGURATION = "generic configuration";
    private static final String KEYWORD_USES = "uses";
    private static final String KEYWORD_PROVIDES = "provides";
    private static final String KEYWORD_INTERFACE = "interface";
    private static final String KEYWORD_AS = "as";
    private static final String KEYWORD_IMPLEMENTATION = "implementation";

    /**
     * Texts that appear in the page.
     */
    private static final String PAGE_NAME = "New NesC component";
    private static final String PAGE_DESCRIPTION = "Create a new NesC component.";
    private static final String NAME_COMPONENT_NAME_FIELD = "Component name";
    private static final String LABEL_COMMENTS_CHECKBOX = "Generate comments";
    private static final String LABEL_GROUP_CHECKBOX = "Group uses/provides";
    private static final String ERR_MSG_COMPONENT_EXISTS = "A component with given name "
            + "already exists in the source folder.";

    /**
     * Fields that this page consists of.
     */
    private SourceFolderField sourceFolderField;
    private IdentifierField componentNameField;
    private ComponentKindField componentKindField;
    private GenericParametersField genericParametersField;
    private UsesProvidesField usesProvidesField;

    /**
     * Array with all fields from this page for simple iteration.
     */
    private WizardField[] fields;

    /**
     * Additional controls that are on this page.
     */
    private Button commentsCheckbox;
    private Button groupCheckbox;

    NescComponentWizardPage() {
        super(PAGE_NAME, PAGE_NAME, NescWizardSupport.getImageDescriptorForResource(ICON_PATH));
        setDescription(PAGE_DESCRIPTION);
    }

    @Override
    public void createControl(Composite parent) {
        // Create composite for this page
        final Composite pageComposite = new Composite(parent, NONE);
        pageComposite.setLayout(new GridLayout());

        // Create all fields for this page
        final GridData layoutData = new GridData(GridData.FILL_HORIZONTAL),
                       centerLayoutData = new GridData(FILL, FILL, true, true);
        sourceFolderField = new SourceFolderField(pageComposite, layoutData,
                NescWizardSupport.getInitialSourceFolderFullPath(), getShell());
        componentNameField = new IdentifierField(pageComposite, NAME_COMPONENT_NAME_FIELD,
                layoutData);
        componentKindField = new ComponentKindField(pageComposite, null);
        genericParametersField = new GenericParametersField(pageComposite, centerLayoutData,
                getShell());
        genericParametersField.setEnabled(false);
        usesProvidesField = new UsesProvidesField(pageComposite, centerLayoutData,
                getShell());
        fields = new WizardField[] { sourceFolderField, componentNameField,
                componentKindField, genericParametersField, usesProvidesField };
        createCheckboxes(pageComposite);

        // Final actions
        sourceFolderField.align(new AbstractField[] { componentNameField, componentKindField,
                genericParametersField, usesProvidesField });
        registerAllListeners();
        setPageComplete(false);
        setControl(pageComposite);
    }

    /**
     * Creates and configures checkboxes that are on this wizard page.
     *
     * @param pageComposite The created checkboxes will be placed in this
     *                      composite.
     */
    private void createCheckboxes(Composite pageComposite) {
        // Create the checkbox for choosing comments generation option
        commentsCheckbox = new Button(pageComposite, CHECK);
        commentsCheckbox.setText(LABEL_COMMENTS_CHECKBOX);
        commentsCheckbox.setSelection(true);

        // Create the checkbox for choosing if uses/provides are to be grouped
        groupCheckbox = new Button(pageComposite, CHECK);
        groupCheckbox.setText(LABEL_GROUP_CHECKBOX);
        groupCheckbox.setSelection(true);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        // Set focus to the component name if the source folder is entered
        if (!sourceFolderField.isEmpty()) {
            componentNameField.setFocus();
        }
    }

    /**
     * @return Only the name of the file that will be created for the
     *         component.
     */
    public String getNewComponentFileName() {
        return componentNameField.getValue() + NescWizardSupport.NESC_SOURCE_EXTENSION;
    }

    /**
     * @return Full path to the new component to be created in the Eclipse
     *         "filesystem".
     */
    public String getNewComponentFullPath() {
        return sourceFolderField.getValue() + getNewComponentFileName();
    }

    /**
     * @return True if and only if the user has chosen to generate comments.
     */
    public boolean getCommentsFlag() {
        return commentsCheckbox.getSelection();
    }

    /**
     * @return True if and only if the user has chosen to group uses/provides
     *         entries.
     */
    public boolean getGroupFlag() {
        return groupCheckbox.getSelection();
    }

    /**
     * @return Initial contents of the new component to be created.
     */
    public NewFileContents getNewComponentContents() {
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(byteOut);

        // Add comments if the user has chosen to
        if (getCommentsFlag()) {
            out.println(NescWizardSupport.generateHeadComment());
            out.println();
        }

        // Write the keyword a name of the component
        final ComponentKindResolver componentInfo = new ComponentKindResolver();
        componentKindField.getValue().accept(componentInfo);
        out.print(componentInfo.componentKindKeyword);
        out.print(' ');
        out.print(componentNameField.getValue());

        // Write the generic parameters if there is a need to
        if (componentInfo.isGeneric) {
            writeGenericParameters(out);
        }

        // Write the uses/provides items
        out.println();
        out.println('{');
        writeUsesProvides(out);
        out.println('}');

        // Write the implementation section
        int cursorOffset = byteOut.size();
        if (componentInfo.containsImplementation) {
            out.println(KEYWORD_IMPLEMENTATION);
            out.println('{');
            out.print(NescWizardSupport.getIndentationStep());
            cursorOffset = byteOut.size();
            out.println();
            out.println('}');
        }

        return new NewFileContents(new ByteArrayInputStream(byteOut.toByteArray()),
                cursorOffset);
    }

    /**
     * Unconditionally writes the text of generic parameters that have been
     * chosen by the user to the given stream. Before or after the parentheses
     * there is no whitespace.
     */
    private void writeGenericParameters(PrintStream out) {
        final String indent = NescWizardSupport.getIndentationStep();
        boolean first = true;

        out.print('(');

        // Iterate over all chosen parameters and write them
        for (GenericParameter param : genericParametersField.getValue()) {
            if (first) {
                first = false;
            } else {
                out.print(',');
            }

            out.println();
            out.print(indent);

            switch (param.getType()) {
            case CONSTANT_PARAM:
                out.print(param.getConstantType());
                out.print(' ');
                out.print(param.getName());
                break;
            case TYPE_PARAM:
                out.print("typedef ");
                out.print(param.getName());
                if (param.getIntegerAttributeFlag()) {
                    out.print(" @integer()");
                }
                if (param.getNumberAttributeFlag()) {
                    out.print(" @number()");
                }
                break;
            }
        }

        // Add a new line if there were some generic parameters
        if (!first) {
            out.println();
        }

        out.print(')');
    }

    /**
     * Writes uses/provides entries to the given stream. They are grouped if the
     * user has chosen such option. Starting and ending braces are not written.
     * Each entry is followed by a newline character.
     */
    private void writeUsesProvides(PrintStream out) {
        if (getGroupFlag()) {
            writeUsesProvidesGrouped(out);
        } else {
            writeUsesProvidesSimple(out);
        }
    }

    /**
     * Writes uses/provides entries to the given stream. All 'provides' entries
     * and 'uses' entries are grouped and written together.
     */
    private void writeUsesProvidesGrouped(PrintStream out) {
        /* Local class that is used to group uses/provides entries. */
        class UsesProvidesResolver implements UsesProvides.Type.Visitor {
            private final List<UsesProvides> uses = new ArrayList<>();
            private final List<UsesProvides> provides = new ArrayList<>();
            private UsesProvides nextEntry;

            @Override
            public void visit(Uses marker) {
                uses.add(nextEntry);
            }

            @Override
            public void visit(Provides marker) {
                provides.add(nextEntry);
            }
        }

        // Group the uses/provides entries
        final UsesProvidesResolver resolver = new UsesProvidesResolver();
        for (UsesProvides item : usesProvidesField.getValue()) {
            resolver.nextEntry = item;
            item.getType().accept(resolver);
        }

        // Write the entries
        writeUsesProvidesGroup(out, KEYWORD_PROVIDES, resolver.provides);
        writeUsesProvidesGroup(out, KEYWORD_USES, resolver.uses);
    }

    /**
     * Writes the given uses/provides group to the given stream. Something is
     * written if and only if the given group is not empty.
     */
    private void writeUsesProvidesGroup(PrintStream out, String keyword, List<UsesProvides> group) {
        if (!group.isEmpty()) {
            final String indent = NescWizardSupport.getIndentationStep();
            final String squareIndent = indent + indent;

            out.println(indent + keyword);
            out.println(indent + "{");

            // Write all entries
            for (UsesProvides item : group) {
                out.print(squareIndent);
                finishUsesProvidesLine(out, item);
            }

            out.println(indent + "}");
        }
    }

    /**
     * Writes uses/provides entries to the given stream without grouping them.
     */
    private void writeUsesProvidesSimple(final PrintStream out) {
        /* Local class that writes the type of the uses/provides entry to
           the stream. */
        class UsesProvidesResolver implements UsesProvides.Type.Visitor {
            @Override
            public void visit(Uses marker) {
                out.print(KEYWORD_USES);
            }
            @Override
            public void visit(Provides marker) {
                out.print(KEYWORD_PROVIDES);
            }
        };
        final UsesProvidesResolver resolver = new UsesProvidesResolver();
        final String indent = NescWizardSupport.getIndentationStep();

        for (UsesProvides item : usesProvidesField.getValue()) {
            out.print(indent);
            item.getType().accept(resolver);
            out.print(' ');
            finishUsesProvidesLine(out, item);
        }
    }

    /**
     * Writes the uses/provides declaration to the given stream for given item.
     * Part of the declaration from the 'interface' keyword till the end is
     * written. A new line character is also written.
     */
    private void finishUsesProvidesLine(PrintStream out, UsesProvides item) {
        out.print(KEYWORD_INTERFACE);
        out.print(' ');
        out.print(item.getInterfaceName());

        if (!item.getInstanceName().isEmpty()) {
            out.print(' ');
            out.print(KEYWORD_AS);
            out.print(' ');
            out.print(item.getInstanceName());
        }

        if (!item.getInstanceParameters().isEmpty()) {
            out.print(item.getInstanceParameters());
        }

        out.println(';');
    }

    /**
     * Checks if all entered data is valid. If not shows appropriate text and
     * sets the page completion status to false.
     */
    private void updateErrorStatus() {
        // Look for an error in individual fields
        for (WizardField wizardField : fields) {
            final String errMsg = wizardField.getErrorStatus();

            if (errMsg != null) {
                setPageComplete(false);
                setErrorMessage(errMsg);
                return;
            }
        }

        // Check if the component already exists
        if (sourceFolderField.fileExists(getNewComponentFileName())) {
            setPageComplete(false);
            setErrorMessage(ERR_MSG_COMPONENT_EXISTS);
            return;
        }

        // No errors have been found so let the user finish
        setPageComplete(true);
        setErrorMessage(null);
    }

    /**
     * This method registers all listeners of the controls that are used by this
     * page.
     */
    private void registerAllListeners() {
        final ModifyListener modifyListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateErrorStatus();
            }
        };

        sourceFolderField.addModifyListener(modifyListener);
        componentNameField.addModifyListener(modifyListener);

        componentKindField.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateFieldsAccessibility();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
    }

    /**
     * Enables and disables fields. It is based on values in other fields.
     */
    private void updateFieldsAccessibility() {
        componentKindField.getValue().accept(new ComponentKindField.ComponentKind.Visitor() {
            @Override public void visit(Module marker) {
                genericParametersField.setEnabled(false);
            }
            @Override
            public void visit(Configuration marker) {
                genericParametersField.setEnabled(false);
            }
            @Override
            public void visit(BinaryComponent marker) {
                genericParametersField.setEnabled(false);
            }
            @Override
            public void visit(GenericModule marker) {
                genericParametersField.setEnabled(true);
            }
            @Override
            public void visit(GenericConfiguration marker) {
                genericParametersField.setEnabled(true);
            }
        });
    }

    /**
     * Class whose objects contain necessary information about a component kind
     * when creating component initial contents.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    private static class ComponentKindResolver implements ComponentKindField.ComponentKind.Visitor {
        private String componentKindKeyword;
        private boolean isGeneric;
        private boolean containsImplementation;

        @Override
        public void visit(Module marker) {
            componentKindKeyword = KEYWORD_MODULE;
            isGeneric = false;
            containsImplementation = true;
        }

        @Override
        public void visit(Configuration marker) {
            componentKindKeyword = KEYWORD_CONFIGURATION;
            isGeneric = false;
            containsImplementation = true;
        }

        @Override
        public void visit(BinaryComponent marker) {
            componentKindKeyword = KEYWORD_BINARY_COMPONENT;
            isGeneric = false;
            containsImplementation = false;
        }

        @Override
        public void visit(GenericModule marker) {
            componentKindKeyword = KEYWORD_GENERIC_MODULE;
            isGeneric = true;
            containsImplementation = true;
        }

        @Override
        public void visit(GenericConfiguration marker) {
            componentKindKeyword = KEYWORD_GENERIC_CONFIGURATION;
            isGeneric = true;
            containsImplementation = true;
        }
    }
}
