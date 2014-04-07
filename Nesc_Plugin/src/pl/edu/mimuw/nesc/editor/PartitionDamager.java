package pl.edu.mimuw.nesc.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.presentation.IPresentationDamager;

public class PartitionDamager implements IPresentationDamager {

	@Override
	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event, boolean documentPartitioningChanged) {
		if (!documentPartitioningChanged && event.getOffset() == partition.getOffset() + partition.getLength()) {
			IRegion lineRegion;
			try {
				lineRegion = event.fDocument.getLineInformationOfOffset(event.getOffset());
				int start = partition.getOffset();
				int end = lineRegion.getOffset() + lineRegion.getLength();
				return new Region(start, end - start);
			} catch (BadLocationException exc) {
				// ignore
			}
		}
		return partition;
	}

	@Override
	public void setDocument(IDocument document) {
	}

}
