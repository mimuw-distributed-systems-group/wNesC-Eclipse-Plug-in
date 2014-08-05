package pl.edu.mimuw.nesc.plugin.projects.util;

import pl.edu.mimuw.nesc.plugin.preferences.util.CommentTextWrapper;
import pl.edu.mimuw.nesc.plugin.preferences.util.NescCommentValidator;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.osgi.service.prefs.BackingStoreException;

import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.*;
import static org.eclipse.swt.SWT.*;

/**
 * Class that represents a wrapper for the comment group control that enables
 * the user to set project-specific settings.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public final class CommentGroupWrapper {
    /**
     * Various texts that appear near the controls.
     */
    private static final String LABEL_INHERIT = "Inherit the template from the global preferences";
    private static final String LABEL_DONTGENERATE = "Don't generate the comment";
    private static final String LABEL_SPECIFIC = "Project specific template";

    /**
     * Constants that contain the indices to <code>radio</code> array.
     */
    private static final int RADIO_INDEX_INHERIT = 0;
    private static final int RADIO_INDEX_DONTGENERATE = 1;
    private static final int RADIO_INDEX_SPECIFIC = 2;

    /**
     * Constants that affect the layout of the group.
     */
    private static final int INDENT_COMMENT_TEXT = 20;
    private static final int HINT_COMMENT_TEXT_HEIGHT = 70;

    /**
     * Array with the listeners to be called when the setting in this wrapper
     * changes.
     */
    private final List<Listener> listeners = new ArrayList<>();

    /**
     * Group that contains the radios and comment text.
     */
    private final Group group;

    /**
     * Radio buttons that allow the user choosing the proper setting.
     */
    private final Button[] radios = new Button[3];

    /**
     * Comment text wrapper that contains the project-specific comment entered
     * by the user.
     */
    private final CommentTextWrapper commentTextWrapper;

    /**
     * Creates all necessary controls and adds them to the given composite.
     * If the given layout object is not null, it is associated with the created
     * group.
     *
     * @param parent Composite that will contain all created controls.
     * @param groupText Text that will be used as the title for the created
     *                  group.
     * @param layoutData Object that will be associated with the created group
     *                   if not null.
     * @throws NullPointerException <code>parent</code> or
     *                              <code>groupText</code> is null.
     */
    public CommentGroupWrapper(Composite parent, String groupText, Object layoutData) {
        // Check arguments
        Preconditions.checkNotNull(parent, "parent cannot be null");
        Preconditions.checkNotNull(groupText, "group text cannot be null");

        // Create and configure the group
        group = new Group(parent, NONE);
        group.setLayout(new GridLayout());
        group.setText(groupText);
        if (layoutData != null) {
            group.setLayoutData(layoutData);
        }

        // Create the contained controls
        createRadios();
        commentTextWrapper = createCommentText();

        // Final actions
        updateEnabledProperty();
        registerListeners();
    }

    /**
     * Creates the radios controls and adds them to the group.
     */
    private void createRadios() {
        radios[RADIO_INDEX_INHERIT] = new Button(group, RADIO);
        radios[RADIO_INDEX_INHERIT].setText(LABEL_INHERIT);
        radios[RADIO_INDEX_INHERIT].setLayoutData(new GridData(FILL, TOP, true, false));

        radios[RADIO_INDEX_DONTGENERATE] = new Button(group, RADIO);
        radios[RADIO_INDEX_DONTGENERATE].setText(LABEL_DONTGENERATE);
        radios[RADIO_INDEX_DONTGENERATE].setLayoutData(new GridData(FILL, TOP, true, false));

        radios[RADIO_INDEX_SPECIFIC] = new Button(group, RADIO);
        radios[RADIO_INDEX_SPECIFIC].setText(LABEL_SPECIFIC);
        radios[RADIO_INDEX_SPECIFIC].setLayoutData(new GridData(FILL, TOP, true, false));

        setRadio(RADIO_INDEX_INHERIT);
    }

    /**
     * @return The newly created comment text wrapper. Its controls are added
     *         to the group composite.
     */
    private CommentTextWrapper createCommentText() {
        final GridData layoutData = new GridData(FILL, FILL, true, true);
        layoutData.heightHint = HINT_COMMENT_TEXT_HEIGHT;
        layoutData.horizontalIndent = INDENT_COMMENT_TEXT;
        return new CommentTextWrapper(group, layoutData);
    }

    /**
     * Registers the listener that will update the enable property of the
     * controls in this wrapper and that will call the registered listeners.
     */
    private void registerListeners() {
        final SelectionListener listener = new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                updateEnabledProperty();
                callListeners();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                // ignore as this method is never called in this context
            }
        };

        // Register listeners
        for (Button radio : radios) {
            radio.addSelectionListener(listener);
        }
        commentTextWrapper.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                callListeners();
            }
        });
    }

    /**
     * Updates the enabled property of the wrapper to reflect current state.
     */
    private void updateEnabledProperty() {
        commentTextWrapper.setEnabled(radios[RADIO_INDEX_SPECIFIC].getSelection());
    }

    /**
     * Calls all registered listeners.
     */
    private void callListeners() {
        synchronized (listeners) {
            for (Listener listener : listeners) {
                listener.change();
            }
        }
    }

    /**
     * Sets the radio option with given index. If a radio with given index
     * does not exist, then all radios are unselected after the call.
     */
    private void setRadio(int radioIndex) {
        for (int i = 0; i < radios.length; ++i) {
            radios[i].setSelection(i == radioIndex);
        }
    }

    /**
     * @return The setting that is currently indicated by the states of the
     *         controls in this wrapper.
     */
    public Setting getSetting() {
        if (radios[RADIO_INDEX_INHERIT].getSelection()) {
            return new Inherit(commentTextWrapper.getContents());
        } else if (radios[RADIO_INDEX_DONTGENERATE].getSelection()) {
            return new DontGenerate(commentTextWrapper.getContents());
        } else {
            return new ProjectSpecific(commentTextWrapper.getContents());
        }
    }

    /**
     * Sets the state of the controls in this wrapper to reflect the given
     * setting.
     *
     * @param setting Setting that will be shown by controls in this wrapper.
     * @throws NullPointerException Given argument is null.
     */
    public void setSetting(Setting setting) {
        Preconditions.checkNotNull(setting, "setting cannot be null");
        setting.accept(new Setting.Visitor() {
            @Override
            public void visit(Inherit marker) {
                setRadio(RADIO_INDEX_INHERIT);
            }

            @Override
            public void visit(DontGenerate marker) {
                setRadio(RADIO_INDEX_DONTGENERATE);
            }

            @Override
            public void visit(ProjectSpecific marker) {
                setRadio(RADIO_INDEX_SPECIFIC);
            }
        });
        commentTextWrapper.setContents(setting.text);
        updateEnabledProperty();
    }

    /**
     * Sets the state of the controls in this wrapper to reflect the inherit
     * setting.
     */
    public void setInherit() {
        setRadio(RADIO_INDEX_INHERIT);
        updateEnabledProperty();
    }

    /**
     * Registers the given listener for notification when the setting contained
     * in this wrapper changes.
     *
     * @param listener Listener to notify.
     * @throws NullPointerException The argument is null.
     */
    public void addListener(Listener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");

        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Saves the setting that is indicate by the current state of the controls
     * in this wrapper to the preferences of the given project. The saved
     * information is associated with the given key.
     *
     * @param project Project whose preferences will contain the setting.
     * @param key Key that will be associated with the saved information in the
     *            preferences.
     * @throws NullPointerException One of the arguments is null.
     */
    public void saveInProjectPreferences(IProject project, String key) throws BackingStoreException {
        Setting.saveInProjectPreferences(getSetting(), project, key);
    }

    /**
     * Sets the state of the controls in this wrapper to indicate the setting
     * that is saved in the given project's preferences and associated there
     * with given key.
     *
     * @param project Project that contains the loaded setting.
     * @param key Key that is associated with the preference of the given
     *            project to load the setting from.
     */
    public void loadFromProjectPreferences(IProject project, String key) {
        setSetting(Setting.loadFromProjectPreferences(project, key));
    }

    /**
     * @return True if and only if the setting from this wrapper is valid, i.e.
     *         the entered comment is valid or the chosen option is other than
     *         "project specific".
     */
    public boolean validate() {
        return getSetting().validate();
    }

    /**
     * Class hierarchy that represents the setting that controls in this wrapper
     * allow to set and change. It follows the Visitor design pattern.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    public static abstract class Setting {
        /**
         * A text that is associated with the setting. Never null.
         */
        protected final String text;

        private Setting(String text) {
            Preconditions.checkNotNull(text, "text cannot be null");
            this.text = text;
        }

        public abstract void accept(Visitor visitor);

        /**
         * @return True if and only if the setting represented by this object is
         *         valid.
         */
        public abstract boolean validate();

        /**
         * Saves the given setting in the preferences of given project using the
         * given key.
         *
         * @param toSave Setting to save.
         * @param project Project whose preferences will contain information
         *                from the given <code>Setting</code> object.
         * @param key Key that will be associated with the saved information.
         * @return True if and only if the operation succeeded.
         * @throws NullPointerException One of the arguments is null.
         */
        public static void saveInProjectPreferences(Setting toSave, IProject project, String key)
                throws BackingStoreException {
            // Check the arguments
            if (toSave == null || project == null || key == null) {
                throw new NullPointerException("one of the arguments is null");
            }

            /* Local class that contains the implementation of the save
               operation. */
            class SaveVisitor implements Visitor {
                private String prefix;

                @Override
                public void visit(Inherit marker) {
                    prefix = "|";
                }

                @Override
                public void visit(DontGenerate marker) {
                    prefix = "#|";
                }

                @Override
                public void visit(ProjectSpecific marker) {
                    prefix = "##|";
                }
            }

            // Perform the operation
            final SaveVisitor visitor = new SaveVisitor();
            toSave.accept(visitor);

            NescProjectPreferences.transaction(project)
            		.set(key, visitor.prefix + toSave.text)
            		.commit();
        }

        /**
         * Loads the setting that controls from this wrapper allow to set from
         * the preferences of the given project and value associated with given
         * key. If no value is associated with the given key the inherit setting
         * is returned.
         *
         * @param project Project with preferences that contain the setting.
         * @param key Key that is associated with value that contains the
         *            information.
         * @return A Setting object that represents information stored in the
         *         given project preferences in the value associated with the
         *         given key.
         * @throws NullPointerException One of the arguments is null.
         */
        public static Setting loadFromProjectPreferences(IProject project, String key) {
            // Check the arguments
            if (project == null || key == null) {
                throw new NullPointerException("null argument");
            }

            // Get the value from the preferences and parse it
            final String value = getProjectPreferenceValue(project, key);
            if (value.startsWith("|")) {
                return new Inherit(value.substring(1));
            } else if (value.startsWith("#|")) {
                return new DontGenerate(value.substring(2));
            } else if (value.startsWith("##|")) {
                return new ProjectSpecific(value.substring(3));
            } else {
                return new Inherit(value);
            }
        }

        public interface Visitor {
            void visit(Inherit marker);
            void visit(DontGenerate marker);
            void visit(ProjectSpecific marker);
        }
    }

    public static final class Inherit extends Setting {
        private Inherit(String text) {
            super(text);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public boolean validate() {
            return true;
        }
    }

    public static final class DontGenerate extends Setting {
        private DontGenerate(String text) {
            super(text);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public boolean validate() {
            return true;
        }
    }

    public static final class ProjectSpecific extends Setting {
        private ProjectSpecific(String comment) {
            super(comment);
        }

        public String getComment() {
            return text;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public boolean validate() {
            return NescCommentValidator.getInstance().validate(getComment());
        }
    }

    /**
     * Interface with a method that is called when the setting contained in
     * this wrapper could have changed. If it changes, this method will
     * be called. If it is called, it does not necessarily mean that the
     * setting has been changed.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    public interface Listener {
        void change();
    }
}
