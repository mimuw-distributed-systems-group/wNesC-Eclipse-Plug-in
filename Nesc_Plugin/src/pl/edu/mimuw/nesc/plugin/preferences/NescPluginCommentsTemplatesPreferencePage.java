package pl.edu.mimuw.nesc.plugin.preferences;

import pl.edu.mimuw.nesc.plugin.preferences.util.CommentTextWrapper;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import static org.eclipse.swt.SWT.*;

/**
 * Class that represents the comments templates page in the NesC preferences.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public final class NescPluginCommentsTemplatesPreferencePage extends NescPreferencePage {
    /**
     * Various texts used in this preference page.
     */
    private static final String LABEL_HEAD_COMMENT = "Head comment:";
    private static final String LABEL_ENTITY_COMMENT = "Interface/component comment:";
    private static final String ERR_MSG_INVALID_HEAD_COMMENT = "Entered head comment is invalid.";
    private static final String ERR_MSG_INVALID_ENTITY_COMMENT = "Entered interface and component comment is invalid";

    /**
     * Constants that determine the sizes of controls on this preference page.
     */
    private static final int TEXT_HEIGHT_HINT = 100;
    private static final int LABEL_TOP_PADDING = 10;

    /**
     * Wrappers for the comment texts.
     */
    private CommentTextWrapper headCommentTextWrapper;
    private CommentTextWrapper entityCommentTextWrapper;

    @Override
    protected Control createContents(Composite parent) {
        // Create and configure the page composite
        final Composite pageComposite = new Composite(parent, NONE);
        final GridLayout pageLayout = new GridLayout();
        pageLayout.marginBottom = pageLayout.marginHeight = pageLayout.marginLeft
                = pageLayout.marginRight = pageLayout.marginTop
                = pageLayout.marginWidth = 0;
        pageComposite.setLayout(pageLayout);

        // Create and set layout objects for Text controls
        final GridData headTextGridData = new GridData(FILL, FILL, true, false),
                       entityTextGridData = new GridData(FILL, FILL, true, false);
        headTextGridData.heightHint = entityTextGridData.heightHint = TEXT_HEIGHT_HINT;

        // Create and set layout objects for Label controls
        final GridData headLabelGridData = new GridData(LEFT, CENTER, false, false),
                       entityLabelGridData = new GridData(LEFT, CENTER, false, false);
        entityLabelGridData.verticalIndent = LABEL_TOP_PADDING;

        // Create controls
        headCommentTextWrapper = createControlsSet(pageComposite, LABEL_HEAD_COMMENT,
                headLabelGridData, headTextGridData);
        entityCommentTextWrapper = createControlsSet(pageComposite, LABEL_ENTITY_COMMENT,
                entityLabelGridData, entityTextGridData);

        // Final actions
        initializeValues();
        registerValidityListeners();

        return pageComposite;
    }

    private void registerValidityListeners() {
        final ModifyListener listener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateErrorStatus();
            }
        };

        headCommentTextWrapper.addModifyListener(listener);
        entityCommentTextWrapper.addModifyListener(listener);
    }

    private void updateErrorStatus() {
        if (!headCommentTextWrapper.validate()) {
            setErrorMessage(ERR_MSG_INVALID_HEAD_COMMENT);
            setValid(false);
            return;
        }

        if (!entityCommentTextWrapper.validate()) {
            setErrorMessage(ERR_MSG_INVALID_ENTITY_COMMENT);
            setValid(false);
            return;
        }

        setErrorMessage(null);
        setValid(true);
    }

    private static CommentTextWrapper createControlsSet(Composite parent, String labelText,
            Object labelLayoutData, Object textLayoutData) {
        // Create label
        final Label label = new Label(parent, NONE);
        label.setText(labelText);
        if (labelLayoutData != null) {
            label.setLayoutData(labelLayoutData);
        }

        // Create the comment text wrapper
        return new CommentTextWrapper(parent, textLayoutData);
    }

    @Override
    protected void initializeDefaults() {
        final IPreferenceStore store = getPreferenceStore();
        headCommentTextWrapper.setContents(store.getDefaultString(NescPluginPreferences.HEAD_COMMENT));
        entityCommentTextWrapper.setContents(store.getDefaultString(NescPluginPreferences.ENTITY_COMMENT));
    }

    @Override
    protected void initializeValues() {
        final IPreferenceStore store = getPreferenceStore();
        headCommentTextWrapper.setContents(store.getString(NescPluginPreferences.HEAD_COMMENT));
        entityCommentTextWrapper.setContents(store.getString(NescPluginPreferences.ENTITY_COMMENT));
    }

    @Override
    protected void storeValues() {
        final IPreferenceStore store = getPreferenceStore();
        store.setValue(NescPluginPreferences.HEAD_COMMENT, headCommentTextWrapper.getContents());
        store.setValue(NescPluginPreferences.ENTITY_COMMENT, entityCommentTextWrapper.getContents());
    }
}
