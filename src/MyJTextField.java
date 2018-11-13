import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class MyJTextField extends PlainDocument {
	
	private final static int limit = 1;
	
	public MyJTextField(){
		super();
	}
	
	@Override
	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException{
		if(str == null){
			return;
		}
		int numeric;
		try {
			numeric = Integer.parseInt(str);
			if((getLength() + str.length()) <= limit){
				super.insertString(offset, str, attr);
			}			
		} catch(NumberFormatException e){
			// Do nothing
		}		
	}
	

}
