package pl.edu.mimuw.nesc.plugin.editor.contentassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.graphics.Image;

import pl.edu.mimuw.nesc.ast.gen.DataDecl;
import pl.edu.mimuw.nesc.ast.gen.Declaration;
import pl.edu.mimuw.nesc.ast.gen.ErrorDecl;
import pl.edu.mimuw.nesc.ast.gen.ExceptionVisitor;
import pl.edu.mimuw.nesc.ast.gen.VariableDecl;
import pl.edu.mimuw.nesc.declaration.nesc.ConfigurationDeclaration;
import pl.edu.mimuw.nesc.declaration.nesc.InterfaceDeclaration;
import pl.edu.mimuw.nesc.declaration.nesc.ModuleDeclaration;
import pl.edu.mimuw.nesc.declaration.nesc.NescDeclaration;
import pl.edu.mimuw.nesc.declaration.object.ComponentRefDeclaration;
import pl.edu.mimuw.nesc.declaration.object.ConstantDeclaration;
import pl.edu.mimuw.nesc.declaration.object.FunctionDeclaration;
import pl.edu.mimuw.nesc.declaration.object.FunctionDeclaration.FunctionType;
import pl.edu.mimuw.nesc.declaration.object.InterfaceRefDeclaration;
import pl.edu.mimuw.nesc.declaration.object.ObjectDeclaration;
import pl.edu.mimuw.nesc.declaration.object.TypenameDeclaration;
import pl.edu.mimuw.nesc.declaration.object.VariableDeclaration;
import pl.edu.mimuw.nesc.environment.Environment;
import pl.edu.mimuw.nesc.environment.ScopeType;
import pl.edu.mimuw.nesc.plugin.editor.ImageManager;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.CommandEventPattern;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.EmptyPatternVisitor;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.IdentifierChainPattern;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.Pattern;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.TaskPattern;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.VariablePattern;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token;

import com.google.common.base.Optional;

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
	private final ScopeType currentScopeType;
	private final List<Pattern> matchingPatterns;

	public ObjectProposalBuilder(int currentOffset, Environment environment, ScopeType currentScopeType,
			List<Pattern> matchingPatterns) {
		this.currentOffset = currentOffset;
		this.environment = environment;
		this.currentScopeType = currentScopeType;
		this.matchingPatterns = matchingPatterns;
	}

	@Override
	public void buildProposals() {
		final ObjectDeclarationVisitor visitor = new ObjectDeclarationVisitor();
		for (Map.Entry<String, ObjectDeclaration> entry : environment.getObjects().getAll()) {
			entry.getValue().accept(visitor, null);
		}
	}

	/**
	 * Helper for appending function parameters to proposal template's name and
	 * pattern.
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 *
	 */
	private class ParametersVisitor extends ExceptionVisitor<Void, Void> {

		private final StringBuilder nameBuilder;
		private final StringBuilder patternBuilder;

		private int paramCount;

		public ParametersVisitor(StringBuilder nameBuilder, StringBuilder patternBuilder, int paramCount) {
			this.nameBuilder = nameBuilder;
			this.patternBuilder = patternBuilder;
			this.paramCount = paramCount;
		}

		public int getParamCount() {
			return paramCount;
		}

		@Override
		public Void visitDataDecl(DataDecl decl, Void arg) {
			List<Declaration> declarations = decl.getDeclarations();
			for (Declaration declaration : declarations) {
				try {
					declaration.accept(this, null);
				} catch (Exception e) {
					// TODO log
					e.printStackTrace();
					appendDefaultParameter();
				}
			}
			return null;
		}

		@Override
		public Void visitErrorDecl(ErrorDecl decl, Void arg) {
			appendDefaultParameter();
			return null;
		}

		@Override
		public Void visitVariableDecl(VariableDecl variableDecl, Void arg) {
			if (variableDecl.getDeclaration() == null) {
				// FIXME: declaration should not be null, but frontend
				// does not handle properly e.g. forward declarations such
				// as "int foo(int, int)";
				appendDefaultParameter();
				return null;
			}
			final String name = variableDecl.getDeclaration().getName();
			appendParameter(name);
			return null;
		}

		private void appendDefaultParameter() {
			appendParameter("arg" + this.paramCount);
		}

		private void appendParameter(String paramName) {
			if (this.paramCount > 0) {
				this.nameBuilder.append(", ");
				this.patternBuilder.append(", ");
			}
			this.nameBuilder.append(paramName);
			this.patternBuilder.append("${" + paramName + "}");
			this.paramCount++;
		}

	}

	/**
	 * Visitors that tries to combine object declaration with matching patterns
	 * to provide relevant completion proposals.
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 *
	 */
	private final class ObjectDeclarationVisitor implements ObjectDeclaration.Visitor<Void, Void> {

		@Override
		public Void visit(final ComponentRefDeclaration declaration, Void arg) {
			if (currentScopeType != ScopeType.CONFIGURATION_IMPLEMENTATION) {
				return null;
			}

			final EmptyPatternVisitor<Void, Void> visitor = new EmptyPatternVisitor<Void, Void>() {

				@Override
				public Void visit(VariablePattern pattern, Void arg) {
					/*
					 * Try to suggest component references.
					 */
					final String name = declaration.getName();
					if (name.startsWith(pattern.getName())) {
						final Image image = imageForScope(ScopeType.SPECIFICATION);
						addProposal(name, pattern.getOffset(), pattern.getLength(), image);
					}
					return null;
				}

				@Override
				public Void visit(IdentifierChainPattern pattern, Void arg) {
					/*
					 * Try to suggest any of component's member, i.e.
					 * interfaces, enum constants, typedefs.
					 *
					 * There are some slightly problematical cases: C1.Iface1 ->
					 * C2.Iface2; This is all recognized as identifier chain but
					 * we take into account only a part of it.
					 */
					final List<Token> tokens = pattern.getTokensList();
					final Optional<Token> lastId = pattern.getLastIdToken();

					final String guessedComponentRefName;
					final String guessedMemberNamePrefix;
					if (lastId.isPresent() && tokens.size() >= 3) {
						guessedComponentRefName = tokens.get(tokens.size() - 3).getValue();
						guessedMemberNamePrefix = lastId.get().getValue();
					} else if (!lastId.isPresent() && tokens.size() >= 2) {
						guessedComponentRefName = tokens.get(tokens.size() - 2).getValue();
						guessedMemberNamePrefix = "";
					} else {
						return null;
					}

					/* Check if name of component reference matches. */
					final String componentRefName = declaration.getName();
					if (!componentRefName.equals(guessedComponentRefName)) {
						return null;
					}

					final Optional<? extends NescDeclaration> componentDeclaration =
							declaration.getComponentDeclaration();
					/* Check if component exists. */
					if (!componentDeclaration.isPresent()) {
						return null;
					}

					/* Extract component's specification environment. */
					final Environment specificationEnvironment = componentDeclaration.get().accept(
							new NescDeclaration.Visitor<Environment, Void>() {
								@Override
								public Environment visit(ConfigurationDeclaration configurationDecl, Void arg) {
									return configurationDecl.getAstComponent().getSpecificationEnvironment();
								}

								@Override
								public Environment visit(InterfaceDeclaration interfaceDecl, Void arg) {
									// TODO: error
									return null;
								}

								@Override
								public Environment visit(ModuleDeclaration moduleDecl, Void arg) {
									return moduleDecl.getAstComponent().getSpecificationEnvironment();
								}
							}, null);

					if (specificationEnvironment == null) {
						return null;
					}

					/* Iterate over all declarations in component specification. */
					final Set<Map.Entry<String, ObjectDeclaration>> specificationEntries = specificationEnvironment
							.getObjects().getAll();
					final Image image = imageForScope(ScopeType.SPECIFICATION);
					for (Map.Entry<String, ObjectDeclaration> entry : specificationEntries) {
						final String name = entry.getKey();
						if (name.startsWith(guessedMemberNamePrefix)) {
							addProposal(name, pattern.getOffset(), pattern.getLength(), image);
						}
					}

					return null;
				}
			};
			iteratePatterns(visitor);
			return null;
		}

		@Override
		public Void visit(final ConstantDeclaration declaration, Void arg) {
			final ScopeType declarationScopeType = declaration.getEnvironment().getScopeType();
			// TODO: determine allowed scopes
			final EmptyPatternVisitor<Void, Void> visitor = new EmptyPatternVisitor<Void, Void>() {

				@Override
				public Void visit(VariablePattern pattern, Void arg) {
					final String name = declaration.getName();
					if (name.startsWith(pattern.getName())) {
						final Image image = imageForScope(declarationScopeType);
						addProposal(name, pattern.getOffset(), pattern.getLength(), image);
					}
					return null;
				}
			};
			iteratePatterns(visitor);
			return null;
		}

		@Override
		public Void visit(final FunctionDeclaration declaration, Void arg) {
			final EmptyPatternVisitor<Void, Void> visitor = new EmptyPatternVisitor<Void, Void>() {
				@Override
				public Void visit(VariablePattern pattern, Void arg) {
					if (currentScopeType != ScopeType.COMPOUND) {
						return null;
					}
					/* Tasks are handled somewhere else. */
					if (declaration.getFunctionType() == FunctionType.TASK) {
						return null;
					}

					final String name = declaration.getName();
					// TODO skip function with present interface
					if (name.startsWith(pattern.getName())) {
						final Template template = buildFunctionTemplate(declaration);
						addNescTemplateProposal(template, currentOffset, pattern.getLength());
					}
					return null;
				}

				@Override
				public Void visit(TaskPattern pattern, Void arg) {
					if (currentScopeType != ScopeType.COMPOUND) {
						return null;
					}
					if (declaration.getFunctionType() != FunctionType.TASK) {
						return null;
					}
					final String name = declaration.getName();
					if (!pattern.getName().isPresent() || name.startsWith(pattern.getName().get())) {
						final Template template = buildTaskTemplate(declaration);
						addNescTemplateProposal(template, currentOffset, pattern.getLength());
					}
					return null;
				}
			};
			iteratePatterns(visitor);
			return null;
		}

		@Override
		public Void visit(final InterfaceRefDeclaration declaration, Void arg) {
			/*
			 * Commands and events can be proposed only inside
			 * functions.
			 */
			if (currentScopeType != ScopeType.COMPOUND) {
				return null;
			}

			final EmptyPatternVisitor<Void, Void> visitor = new EmptyPatternVisitor<Void, Void>() {
				@Override
				public Void visit(CommandEventPattern pattern, Void arg) {
					/*
					 * Probably due to syntax errors, ast tree for component was
					 * not built. Skip.
					 */
					if (declaration.getIfaceDeclaration() == null) {
						return null;
					}
					/* Probably there was mistake in interface name. */
					if (!declaration.getIfaceDeclaration().isPresent()) {
						return null;
					}
					final InterfaceDeclaration ifaceDeclaration = declaration.getIfaceDeclaration().get();
					final boolean isProvides = declaration.isProvides();

					/* Check if interface reference name matches. */
					final String refName = declaration.getName();
					if (pattern.getInterfaceName().isPresent() && !refName.startsWith(pattern.getInterfaceName().get())) {
						return null;
					}

					/* Find all matching event/commands. */
					final Environment ifaceEnvironment = ifaceDeclaration.getDeclarationEnvironment();
					final List<FunctionDeclaration> allowedFunctions = new ArrayList<>();

					for (Map.Entry<String, ObjectDeclaration> entry : ifaceEnvironment.getObjects().getAll()) {
						if (!(entry.getValue() instanceof FunctionDeclaration)) {
							continue;
						}

						final FunctionDeclaration funDeclaration = (FunctionDeclaration) entry.getValue();
						final String funName = funDeclaration.getFunctionName();

						final boolean funNameMatches = (!pattern.getFunctionName().isPresent())
								|| (pattern.getFunctionName().isPresent() && funName.startsWith(pattern
										.getFunctionName().get()));
						final boolean isAllowedType = isAllowedFunctionType(funDeclaration.getFunctionType(),
								pattern.getType(), isProvides);

						if (funNameMatches && isAllowedType) {
							allowedFunctions.add(funDeclaration);
						}
					}

					final List<Template> templates = buildTemplatesFromInterfaceReference(ifaceDeclaration,
							declaration, allowedFunctions);

					for (Template template : templates) {
						addNescTemplateProposal(template, currentOffset, pattern.getLength());
					}
					return null;
				}
			};
			iteratePatterns(visitor);
			return null;
		}

		@Override
		public Void visit(TypenameDeclaration declaration, Void arg) {
			// TODO
			return null;
		}

		@Override
		public Void visit(final VariableDeclaration declaration, Void arg) {
			final ScopeType declarationScopeType = declaration.getEnvironment().getScopeType();
			// TODO: determine allowed scopes
			final EmptyPatternVisitor<Void, Void> visitor = new EmptyPatternVisitor<Void, Void>() {

				@Override
				public Void visit(VariablePattern pattern, Void arg) {
					final String name = declaration.getName();
					if (name.startsWith(pattern.getName())) {
						final Image image = imageForScope(declarationScopeType);
						addProposal(name, pattern.getOffset(), pattern.getLength(), image);
					}
					return null;
				}
			};
			iteratePatterns(visitor);
			return null;
		}

		/**
		 * Iterates over matching patterns and applies given visitor on them.
		 *
		 * @param visitor
		 *            visitor to be executed
		 */
		private <A> void iteratePatterns(Pattern.Visitor<Void, A> visitor) {
			for (Pattern pattern : matchingPatterns) {
				pattern.accept(visitor, null);
			}
		}

		/**
		 * Adds parameters to template's name and pattern.
		 *
		 * @param declarations
		 *            parameter declaration list
		 * @param nameBuilder
		 *            name string builder
		 * @param patternBuilder
		 *            pattern string builder
		 */
		private void addParams(List<Declaration> declarations, StringBuilder nameBuilder, StringBuilder patternBuilder) {
			int paramCount = 0;
			for (Declaration declaration : declarations) {
				final ParametersVisitor visitor = new ParametersVisitor(nameBuilder, patternBuilder, paramCount);
				declaration.accept(visitor, null);
				paramCount = visitor.getParamCount();
			}
		}

		/**
		 * Build function proposal template from given function declaration.
		 *
		 * @param declaration
		 *            function declaration
		 * @return function proposal template
		 */
		private Template buildFunctionTemplate(FunctionDeclaration declaration) {
			return buildFunctionTemplate("", declaration);
		}

		/**
		 * Build task proposal template from given function declaration.
		 *
		 * @param declaration
		 *            task declaration
		 * @return task proposal template
		 */
		private Template buildTaskTemplate(FunctionDeclaration declaration) {
			return buildFunctionTemplate("post ", declaration);
		}

		/**
		 * Build function proposal template from given function declaration.
		 *
		 * @param prefix
		 *            prefix of proposal name and pattern
		 * @param declaration
		 *            function declaration
		 * @return function proposal template
		 */
		private Template buildFunctionTemplate(String prefix, FunctionDeclaration declaration) {
			final String name = declaration.getName();
			final StringBuilder nameBuilder = new StringBuilder();
			final String templateDesc = "";
			final String contextTypeId = NescContextType.NESC_CONTEXT_TYPE;
			final StringBuilder patternBuilder = new StringBuilder();

			nameBuilder.append(prefix);
			patternBuilder.append(prefix);

			nameBuilder.append(name);
			nameBuilder.append('(');
			patternBuilder.append(name);
			patternBuilder.append('(');

			addParams(declaration.getAstFunctionDeclarator().getParameters(), nameBuilder, patternBuilder);

			nameBuilder.append(')');
			patternBuilder.append(")${cursor}");

			final Image image = imageForScope(declaration.getEnvironment().getScopeType());
			return buildTemplate(nameBuilder.toString(), templateDesc, contextTypeId, patternBuilder.toString(), image);
		}

		/**
		 * Checks if given function type (command, event) should be suggested
		 * for given completion context (i.e. prefix of call/signal or empty
		 * prefix was recognized) and for given interface reference (uses or
		 * provides).
		 *
		 * @param actual
		 *            type of currently investigated command/event declaration
		 * @param allowed
		 *            type that can be accepted
		 * @param isProvides
		 *            indicates whether referenced interface is used or provided
		 * @return <code>true</code> if command/event can be suggested
		 */
		private boolean isAllowedFunctionType(FunctionDeclaration.FunctionType actual,
				CommandEventPattern.FunctionType allowed, boolean isProvides) {
			if (actual == FunctionDeclaration.FunctionType.COMMAND && !isProvides) {
				return (allowed == CommandEventPattern.FunctionType.COMMAND || allowed == CommandEventPattern.FunctionType.ANY);
			}
			if (actual == FunctionDeclaration.FunctionType.EVENT && isProvides) {
				return (allowed == CommandEventPattern.FunctionType.EVENT || allowed == CommandEventPattern.FunctionType.ANY);
			}
			return false;
		}

		private List<Template> buildTemplatesFromInterfaceReference(InterfaceDeclaration ifaceDeclaration,
				InterfaceRefDeclaration ifaceRefDeclaration, List<FunctionDeclaration> funDeclarations) {
			final List<Template> result = new ArrayList<>();

			final boolean isParameterised = ifaceRefDeclaration.getAstInterfaceRef().getGenericParameters().isPresent();

			for (FunctionDeclaration funDecl : funDeclarations) {
				final StringBuilder nameBuilder = new StringBuilder();
				final StringBuilder patternBuilder = new StringBuilder();

				if (funDecl.getFunctionType() == FunctionDeclaration.FunctionType.COMMAND) {
					nameBuilder.append("call ");
					patternBuilder.append("call ");
				} else {
					nameBuilder.append("signal ");
					patternBuilder.append("signal ");
				}

				nameBuilder.append(ifaceRefDeclaration.getName());
				patternBuilder.append(ifaceRefDeclaration.getName());
				nameBuilder.append('.');
				patternBuilder.append('.');
				nameBuilder.append(funDecl.getFunctionName());
				patternBuilder.append(funDecl.getFunctionName());

				if (isParameterised) {
					nameBuilder.append('[');
					patternBuilder.append('[');
					addParams(ifaceRefDeclaration.getAstInterfaceRef().getGenericParameters().get(), nameBuilder,
							patternBuilder);
					nameBuilder.append(']');
					patternBuilder.append(']');
				}

				nameBuilder.append('(');
				patternBuilder.append('(');
				addParams(funDecl.getAstFunctionDeclarator().getParameters(), nameBuilder, patternBuilder);
				nameBuilder.append(')');
				patternBuilder.append(")${cursor}");

				// TODO: description
				final String desc = ""; // buildLocationDescription(funDecl);
				final Template template = buildTemplate(nameBuilder.toString(), desc,
						NescContextType.NESC_CONTEXT_TYPE, patternBuilder.toString(), ImageManager.COMPLETION_COMPONENT);
				result.add(template);
			}
			return result;
		}
	}
}
