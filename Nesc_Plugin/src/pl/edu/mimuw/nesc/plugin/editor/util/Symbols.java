package pl.edu.mimuw.nesc.plugin.editor.util;

public interface Symbols {
	int TokenEOF= -1; // EOF
	int TokenLBRACE= 1; // {
	int TokenRBRACE= 2; // }
	int TokenLBRACKET= 3; // [
	int TokenRBRACKET= 4; // ]
	int TokenLPAREN= 5; // (
	int TokenRPAREN= 6; // )
	int TokenSEMICOLON= 7; // ;
	int TokenOTHER= 8;
	int TokenCOLON= 9; // :
	int TokenQUESTIONMARK= 10; // ?
	int TokenCOMMA= 11; // ,
	int TokenEQUAL= 12; // =
	int TokenLESSTHAN= 13; // <
	int TokenGREATERTHAN= 14; // >
	int TokenDOT= 15; // .
	int TokenMINUS= 16; // -
	int TokenTILDE= 17; // ~
	int TokenSHIFTRIGHT= 18; // >>
	int TokenARROW= 19; // ->
	int TokenDOUBLECOLON= 20; // ::
	int TokenSHIFTLEFT= 21; // <<
	int TokenPLUS= 22; // +
	int TokenIF= 109; // if
	int TokenDO= 1010; // do
	int TokenFOR= 1011; // for
	//int TokenTRY= 1012; // try x
	int TokenCASE= 1013; // case
	int TokenELSE= 1014; // else
	int TokenBREAK= 1015; // break
	//int TokenCATCH= 1016; // catch x
	int TokenWHILE= 1017; // while
	int TokenRETURN= 1018; // return
	int TokenSTATIC= 1019; // static
	int TokenSWITCH= 1020; // switch
	int TokenGOTO= 1021; // goto
	int TokenDEFAULT= 1022; // default
	//int TokenPRIVATE= 1023; // private x
	//int TokenPROTECTED= 1024; // protected x
	//int TokenPUBLIC= 1025; // public x
	//int TokenNEW= 1026; // new x
	//int TokenDELETE= 1027; // delete x
	//int TokenCLASS= 1028; // class x
	int TokenSTRUCT= 1029; // struct
	int TokenUNION= 1030; // union
	int TokenENUM= 1031; // enum
	//int TokenVIRTUAL= 1032; // virtual x
	//int TokenNAMESPACE= 1033; // namespace x
	//int TokenOPERATOR= 1034; // operator x
	//int TokenTHROW= 1035; // throw x
	int TokenCONST= 1036; // const
	int TokenEXTERN= 1037; // extern ?
	int TokenTYPEDEF= 1038; // typedef
	//int TokenUSING= 1039; // using x
	//int TokenTEMPLATE= 1040; // template x
	//int TokenTYPENAME= 1041; // typename x
	int TokenIDENT= 2000; // ident
	// 3000+ are nesc symbols
	int TokenINTERFACE = 3000;
	int TokenPROVIDES = 3001;
	int TokenUSES = 3002;
	int TokenSTAR = 3003; // *
	int TokenREF = 3004; // &
}
