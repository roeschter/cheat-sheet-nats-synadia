package com.roeschter.pdfbox;

public class TestRender {

	public static void main(String[] args) throws Exception {

		//Test render from diffrent location
		CheatsheetFormatter.main( new String[] {"C:\\temp\\cheatsheet\\NATS_CLI_Cheatsheet.json", "-view" }  );



		//CheatsheetFormatter.main( new String[] {"NATS_Cheatsheet.json", "-view" }  );
		//CheatsheetFormatter.main( new String[] {"NATS_CLI_Cheatsheet.json", "-view" }  );

		//CheatsheetFormatter.main( new String[] {"NATS_CLI_Cheatsheet_2.json", "-view" }  );
		//CheatsheetFormatter.main( new String[] {"Synadia_Value_Proposition.json", "-view" }  );

	}

}
