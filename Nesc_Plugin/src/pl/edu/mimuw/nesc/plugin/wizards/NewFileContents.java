package pl.edu.mimuw.nesc.plugin.wizards;

import java.io.InputStream;

/**
 * Class whose objects contain information needed in the operation of creating
 * and adding new files to a nesC project.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public final class NewFileContents {
    /**
     * Stream that contains the initial contents of the file to be created.
     */
    public final InputStream contents;

    /**
     * Position of the cursor to be set after creating the file.
     */
    public final int cursorOffset;

    /**
     * Initializes the object with given arguments.
     *
     * @param contents Stream with initial file contents.
     * @param cursorOffset Cursor position to set.
     * @throws NullPointerException The first argument is null.
     * @throws IllegalArgumentException Cursor offset is negative.
     */
    NewFileContents(InputStream contents, int cursorOffset) {
        if (contents == null) {
            throw new NullPointerException("NewFileContents.<init>: null stream");
        } else if (cursorOffset < 0) {
            throw new IllegalArgumentException("NewFileContents.<init>: negative cursor offset");
        }

        this.contents = contents;
        this.cursorOffset = cursorOffset;
    }
}