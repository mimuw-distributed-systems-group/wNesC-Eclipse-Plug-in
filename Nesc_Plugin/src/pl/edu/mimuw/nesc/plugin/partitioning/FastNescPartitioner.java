package pl.edu.mimuw.nesc.plugin.partitioning;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

/* Adapted from CDT */

public class FastNescPartitioner extends FastPartitioner {
	
	public FastNescPartitioner() {
		super(new FastNescPartitionScanner(), INCPartitions.ALL_CPARTITIONS);
	}

	public FastNescPartitioner(IPartitionTokenScanner scanner, String[] legalContentTypes) {
		super(scanner, legalContentTypes);
	}
	
	@Override
	public ITypedRegion getPartition(int offset, boolean preferOpenPartitions) {
		if (preferOpenPartitions && offset == fDocument.getLength() && offset > 0) {
			ITypedRegion region = super.getPartition(offset - 1, false);
			try {
				if (INCPartitions.NC_MULTI_LINE_COMMENT.equals(region.getType())) {
					if (!fDocument.get(offset - 2, 2).equals("*/")) { //$NON-NLS-1$
						return region;
					}
				} else if (INCPartitions.NC_SINGLE_LINE_COMMENT.equals(region.getType())) {
					if (fDocument.getChar(offset - 1) != '\n') {
						return region;
					}
				} else if (INCPartitions.NC_PREPROCESSOR.equals(region.getType())) {
					if (fDocument.getChar(offset - 1) != '\n') {
						return region;
					}
				}
			} catch (BadLocationException exc) {
			}
		}
		return super.getPartition(offset, preferOpenPartitions);
	}

}
