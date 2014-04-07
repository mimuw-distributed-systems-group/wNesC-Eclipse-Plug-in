package pl.edu.mimuw.nesc.partitioning;

public interface INCPartitions {
	/**
	 * The identifier of the C partitioning.
	 */
	String NC_PARTITIONING= "___nc_partitioning";  //$NON-NLS-1$

	/**
	 * The identifier of the single-line comment partition content type.
	 */
	String NC_SINGLE_LINE_COMMENT= "__nc_singleline_comment"; //$NON-NLS-1$

	/**
	 * The identifier multi-line comment partition content type.
	 */
	String NC_MULTI_LINE_COMMENT= "__nc_multiline_comment"; //$NON-NLS-1$

	/**
	 * The identifier of the C string partition content type.
	 */
	String NC_STRING= "__nc_string"; //$NON-NLS-1$

	/**
	 * The identifier of the C character partition content type.
	 */
	String NC_CHARACTER= "__nc_character";  //$NON-NLS-1$

	/**
	 * The identifier of the C preprocessor partition content type.
	 */
	String NC_PREPROCESSOR= "__nc_preprocessor";  //$NON-NLS-1$
	
	/**
	 * The identifier of the single-line documentation tool comment partition content type.
     * @since 5.0
	 */
	//String NC_SINGLE_LINE_DOC_COMMENT= "__nc_singleline_doc_comment"; //$NON-NLS-1$

	/**
	 * The identifier multi-line comment documentation tool partition content type.
     * @since 5.0
	 */
	//String NC_MULTI_LINE_DOC_COMMENT= "__nc_multiline_doc_comment"; //$NON-NLS-1$
	
	/**
	 * All defined CDT editor partitions.
	 * @since 5.0
	 */
	String[] ALL_CPARTITIONS= {
			INCPartitions.NC_MULTI_LINE_COMMENT,
			INCPartitions.NC_SINGLE_LINE_COMMENT,
			INCPartitions.NC_STRING,
			INCPartitions.NC_CHARACTER,
			INCPartitions.NC_PREPROCESSOR,
			//INCPartitions.NC_SINGLE_LINE_DOC_COMMENT,
			//INCPartitions.NC_MULTI_LINE_DOC_COMMENT
	};
	
	/**
	 * Array of all assembly partitions.
	 * @since 5.1
	 */
	String[] ALL_ASM_PARTITIONS= new String[] {
			INCPartitions.NC_MULTI_LINE_COMMENT,
			INCPartitions.NC_SINGLE_LINE_COMMENT,
			INCPartitions.NC_STRING,
			INCPartitions.NC_CHARACTER,
			INCPartitions.NC_PREPROCESSOR
	};

}
