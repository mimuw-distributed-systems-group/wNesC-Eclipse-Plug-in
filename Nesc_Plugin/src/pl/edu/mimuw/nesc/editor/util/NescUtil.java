package pl.edu.mimuw.nesc.editor.util;

import pl.edu.mimuw.nesc.ContextRef;
import pl.edu.mimuw.nesc.FileData;
import pl.edu.mimuw.nesc.Frontend;

public class NescUtil {
	
	public static FileData getFileData(Frontend frontend, ContextRef context, String file) {
		try {			
			return frontend.update(context, file);
		} catch (Exception e) {
			return null;
		}
	}

}
