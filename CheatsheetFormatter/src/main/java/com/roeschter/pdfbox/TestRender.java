package com.roeschter.pdfbox;

public class TestRender {

	public static void main(String[] args) throws Exception {

		CheatsheetFormatter.main( new String[] {"-c", "NATS_Cheatsheet.json", "-view" }  );
		//CheatsheetFormatter.main( new String[] {"-c", "NATS_CLI_Cheatsheet.json", "-view" }  );

	}

}
