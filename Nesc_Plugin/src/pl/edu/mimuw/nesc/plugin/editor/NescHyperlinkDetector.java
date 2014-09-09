package pl.edu.mimuw.nesc.plugin.editor;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.common.base.Optional;

import pl.edu.mimuw.nesc.FileData;
import pl.edu.mimuw.nesc.plugin.NescPlugin;
import pl.edu.mimuw.nesc.plugin.frontend.FrontendManager;
import pl.edu.mimuw.nesc.plugin.partitioning.INCPartitions;
import pl.edu.mimuw.nesc.preprocessor.directive.DefineDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.ElifDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.ElseDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.EndifDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.ErrorDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.IfDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.IfdefDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.IfndefDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.IncludeDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.LineDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.PragmaDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.PreprocessorDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.PreprocessorDirective.TokenLocation;
import pl.edu.mimuw.nesc.preprocessor.directive.UndefDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.UnknownDirective;
import pl.edu.mimuw.nesc.preprocessor.directive.WarningDirective;

public class NescHyperlinkDetector extends AbstractHyperlinkDetector {
	private static class DirectiveVisitor implements PreprocessorDirective.Visitor<Boolean, Void> {

		@Override
		public Boolean visit(IncludeDirective arg0, Void arg1) {
			return true;
		}

		@Override
		public Boolean visit(DefineDirective arg0, Void arg1) {
			return false;
		}

		@Override
		public Boolean visit(UndefDirective arg0, Void arg1) {
			return false;
		}

		@Override
		public Boolean visit(IfDirective arg0, Void arg1) {
			return false;
		}

		@Override
		public Boolean visit(IfdefDirective arg0, Void arg1) {
			return false;
		}

		@Override
		public Boolean visit(IfndefDirective arg0, Void arg1) {
			return false;
		}

		@Override
		public Boolean visit(ElseDirective arg0, Void arg1) {
			return false;
		}

		@Override
		public Boolean visit(ElifDirective arg0, Void arg1) {
			return false;
		}

		@Override
		public Boolean visit(EndifDirective arg0, Void arg1) {
			return false;
		}

		@Override
		public Boolean visit(ErrorDirective arg0, Void arg1) {
			return false;
		}

		@Override
		public Boolean visit(WarningDirective arg0, Void arg1) {
			return false;
		}

		@Override
		public Boolean visit(PragmaDirective arg0, Void arg1) {
			return false;
		}

		@Override
		public Boolean visit(LineDirective arg0, Void arg1) {
			return false;
		}

		@Override
		public Boolean visit(UnknownDirective arg0, Void arg1) {
			return false;
		}
		
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		ITextEditor textEditor= (ITextEditor) getAdapter(ITextEditor.class);
		if (region == null || !(textEditor instanceof NescEditor)) {
			return null;
		}
		
		NescEditor nescEditor = (NescEditor) textEditor;
		IDocument doc = nescEditor.getDocument();
		
		try {
			String partition = TextUtilities.getContentType(doc, IDocumentExtension3.DEFAULT_PARTITIONING, region.getOffset(), false);
			if (INCPartitions.NC_PREPROCESSOR.equals(partition)) {
				int line = doc.getLineOfOffset(region.getOffset());
				
				IProject project = nescEditor.getOpenFileProject();
				
				Optional<FileData> fileData = FrontendManager.getFileData(project, nescEditor.getFileLocation());
				if (!fileData.isPresent()) {
					return null;
				}
				List<PreprocessorDirective> directives = fileData.get().getPreprocessorDirectives();

				if (directives != null) {
					for(PreprocessorDirective directive : directives) {
						if (directive.getLineRange().getStart() <= line + 1 && line + 1 <= directive.getLineRange().getEnd()) {
							if (directive.accept(new DirectiveVisitor(), null)) {
								IncludeDirective include = (IncludeDirective) directive;
								
								TokenLocation location = include.getArgumentLocation();
								Region linkRegion = new Region(doc.getLineOffset(line) + location.getColumn(), location.getLength()-2);
								return new IHyperlink[] {new NescElementHyperlink(linkRegion, include, nescEditor)};
							}
						}
					}
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
		region.getOffset();
		
		return null;
	}

}
