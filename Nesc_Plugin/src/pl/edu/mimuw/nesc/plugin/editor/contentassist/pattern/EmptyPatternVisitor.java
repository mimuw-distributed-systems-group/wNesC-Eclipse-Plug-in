package pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern;

/**
 * Pattern visitor that does nothing.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 * @param <R>
 * @param <A>
 */
public class EmptyPatternVisitor<R, A> implements Pattern.Visitor<R, A> {

	@Override
	public R visit(VariablePattern pattern, A arg) {
		return null;
	}

	@Override
	public R visit(CommandEventPattern pattern, A arg) {
		return null;
	}

	@Override
	public R visit(TaskPattern pattern, A arg) {
		return null;
	}

	@Override
	public R visit(IdentifierChainPattern pattern, A arg) {
		return null;
	}
}
