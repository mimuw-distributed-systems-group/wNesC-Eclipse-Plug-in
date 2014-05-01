package pl.edu.mimuw.nesc.plugin.editor.contentassist;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.graphics.Image;

import pl.edu.mimuw.nesc.declaration.object.ComponentRefDeclaration;
import pl.edu.mimuw.nesc.declaration.object.ConstantDeclaration;
import pl.edu.mimuw.nesc.declaration.object.FunctionDeclaration;
import pl.edu.mimuw.nesc.declaration.object.InterfaceRefDeclaration;
import pl.edu.mimuw.nesc.declaration.object.ObjectDeclaration;
import pl.edu.mimuw.nesc.declaration.object.TypenameDeclaration;
import pl.edu.mimuw.nesc.declaration.object.VariableDeclaration;
import pl.edu.mimuw.nesc.environment.Environment;
import pl.edu.mimuw.nesc.environment.ScopeType;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.EmptyPatternVisitor;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.Pattern;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.VariablePattern;

/**
 * Proposal builder of object declarations (variables, functions, enum
 * constants, etc).
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
class ObjectProposalBuilder extends ProposalBuilder {

	private final int currentOffset;
	private final Environment environment;
	private final List<Pattern> matchingPatterns;

	public ObjectProposalBuilder(int currentOffset, Environment environment, List<Pattern> matchingPatterns) {
		super();
		this.currentOffset = currentOffset;
		this.environment = environment;
		this.matchingPatterns = matchingPatterns;
	}

	@Override
	public void buildProposals() {
		final ObjectDeclarationVisitor visitor = new ObjectDeclarationVisitor();
		for (Map.Entry<String, ObjectDeclaration> entry : environment.getObjects().getAll()) {
			entry.getValue().accept(visitor, null);
		}
	}

	private final class ObjectDeclarationVisitor implements ObjectDeclaration.Visitor<Void, Void> {

		@Override
		public Void visit(ComponentRefDeclaration declaration, Void arg) {
			// TODO
			return null;
		}

		@Override
		public Void visit(final ConstantDeclaration declaration, Void arg) {
			final ScopeType currentScopeType = declaration.getEnvironment().getScopeType();
			// TODO: determine allowed scopes
			final EmptyPatternVisitor<Void, Void> visitor = new EmptyPatternVisitor<Void, Void>() {

				@Override
				public Void visit(VariablePattern pattern, Void arg) {
					final String name = declaration.getName();
					if (name.startsWith(pattern.getName())) {
						final Image image = imageForScope(currentScopeType);
						buildProposal(name, pattern.getOffset(), pattern.getLength(), image);
					}
					return null;
				}
			};
			iteratePatterns(visitor);
			return null;
		}

		@Override
		public Void visit(final FunctionDeclaration declaration, Void arg) {
			final ScopeType currentScopeType = declaration.getEnvironment().getScopeType();
			// TODO: determine allowed scopes
			final EmptyPatternVisitor<Void, Void> visitor = new EmptyPatternVisitor<Void, Void>() {

				@Override
				public Void visit(VariablePattern pattern, Void arg) {
					final String name = declaration.getName();
					if (name.startsWith(pattern.getName())) {
						final Template template = buildFunctionTemplate(declaration);
						buildNescTemplateProposal(template, currentOffset, pattern.getLength());
					}
					return null;
				}
			};
			iteratePatterns(visitor);
			return null;
		}

		@Override
		public Void visit(InterfaceRefDeclaration declaration, Void arg) {
			// nothing to do
			return null;
		}

		@Override
		public Void visit(TypenameDeclaration declaration, Void arg) {
			// TODO
			return null;
		}

		@Override
		public Void visit(final VariableDeclaration declaration, Void arg) {
			final ScopeType currentScopeType = declaration.getEnvironment().getScopeType();
			// TODO: determine allowed scopes
			final EmptyPatternVisitor<Void, Void> visitor = new EmptyPatternVisitor<Void, Void>() {

				@Override
				public Void visit(VariablePattern pattern, Void arg) {
					final String name = declaration.getName();
					if (name.startsWith(pattern.getName())) {
						final Image image = imageForScope(currentScopeType);
						buildProposal(name, pattern.getOffset(), pattern.getLength(), image);
					}
					return null;
				}
			};
			iteratePatterns(visitor);
			return null;
		}

		private <A> void iteratePatterns(Pattern.Visitor<Void, A> visitor) {
			for (Pattern pattern : matchingPatterns) {
				pattern.accept(visitor, null);
			}
		}

		private Template buildFunctionTemplate(FunctionDeclaration declaration) {
			final String name = declaration.getName();
			final String templateName = name + "()";
			final String templateDesc = "";
			final String contextTypeId = NescContextType.NESC_CONTEXT_TYPE;
			final String pattern = name + "(${})${cursor}";
			final Image image = imageForScope(declaration.getEnvironment().getScopeType());
			return buildTemplate(templateName, templateDesc, contextTypeId, pattern, image);
		}
	}
}
