import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Stack;

public class GameScreen {

	private JFrame frame;
	private Choice choice;
	
	private ArrayList<JTextField> fields;

	private HashMap<Integer,ArrayList<ArrayList<Integer>>> sudokus;
	
	private Boolean manual = true;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new GameScreen();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GameScreen() {
		initialize();
		frame.setVisible(true);
		loadSudokus();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JButton btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				newGame();
			}
		});
		menuBar.add(btnNew);
		
		JButton btnSolve = new JButton("Solve");
		btnSolve.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				solve();
			}
		});
		menuBar.add(btnSolve);
		
		JTextField textFieldDifficulty = new JTextField();
		textFieldDifficulty.setText("Difficulty:");
		textFieldDifficulty.setEnabled(false);
		textFieldDifficulty.setEditable(false);
		menuBar.add(textFieldDifficulty);
		
		choice = new Choice();
		choice.add("1");
		choice.add("2");
		choice.add("3");
		menuBar.add(choice);
		
		frame.getContentPane().setLayout(new GridLayout(9,9,1,1));		
	}
	
	private void newGame(){
		int level = Integer.parseInt(choice.getSelectedItem());
		ArrayList<ArrayList<Integer>> levelSudokus =  sudokus.get(level);
		if(levelSudokus.size() == 0){
			JOptionPane.showMessageDialog(frame, "No sudoku of level " + Integer.toString(level) + ".", "Error!", JOptionPane.ERROR_MESSAGE);
		} else {
			Random rng = new Random();
			ArrayList<Integer> sudoku = levelSudokus.get(rng.nextInt(levelSudokus.size()));
			frame.getContentPane().removeAll();
			fields = new ArrayList<JTextField>();
			for(Integer number : sudoku){
				JTextField box = new JTextField();
				box.setDocument(new MyJTextField());
				box.setHorizontalAlignment(JTextField.CENTER);
				if(number != 0){
					box.setText(number.toString());
					box.setEditable(false);
					box.setFocusable(false);
				} else {
					box.getDocument().addDocumentListener(new DocumentListener() {
						@Override
						public void insertUpdate(DocumentEvent e) {
							if(manual && isFinished()){
								if(isCorrect()){
									JOptionPane.showMessageDialog(frame, "Sudoku finished, congratulations.", "Done!", JOptionPane.PLAIN_MESSAGE);			
								} else {
									JOptionPane.showMessageDialog(frame, "Sudoku contains mistakes.", "Error!", JOptionPane.ERROR_MESSAGE);							
								}
							}
						}
						@Override
						public void removeUpdate(DocumentEvent e) {}
						@Override
						public void changedUpdate(DocumentEvent e) {}
					});
				}
				frame.getContentPane().add(box);
				fields.add(box);
			}
			frame.getContentPane().revalidate();
			manual = true;
		}
	}
	
	private void solve(){
		if(fields == null){
			JOptionPane.showMessageDialog(frame, "No sudoku loaded! ", "Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		manual = false;
		// Setup variables
		HashMap<JTextField, ArrayList<Integer>> possibleValues = new HashMap<JTextField,ArrayList<Integer>>();
		for(JTextField field : fields){
			if(field.getText().isEmpty()){
				// TODO remove impossible values
				ArrayList<Integer> possibleNumbers = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));
				possibleValues.put(field, possibleNumbers);
			}
		}
		// setup stack to push pop & choices to backtrack
		HashMap<JTextField,ArrayList<Integer>> choices = new HashMap<JTextField,ArrayList<Integer>>();
		for(JTextField field : possibleValues.keySet()){
			choices.put(field, new ArrayList<Integer>(possibleValues.get(field)));
		}
		Stack<JTextField> order = new Stack<JTextField>();
		// fill stack & setup first choices
		for(JTextField field : choices.keySet()){
			int numberToSet = choices.get(field).remove(0);
			field.setText(Integer.toString(numberToSet));
			order.push(field);
		}		
		// Backtrack through variables
		int counter = 0;
		int counterLimit = 999;
		while(!isCorrect() && counter <= counterLimit){
			counter++;
			Stack<JTextField> tempStack = new Stack<JTextField>();
			JTextField fieldToChange = order.pop();
			while(choices.get(fieldToChange).isEmpty()){
				if(order.isEmpty()){
					JOptionPane.showMessageDialog(frame, "Soduku unsolvable! ", "Error!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				tempStack.push(fieldToChange);
				fieldToChange = order.pop();
			}
			ArrayList<Integer> possibleValuesLeft = choices.get(fieldToChange);
			int numberToSet = possibleValuesLeft.remove(0);
			fieldToChange.setText(Integer.toString(numberToSet));
			order.push(fieldToChange);
			while(!tempStack.isEmpty()){
				JTextField field = tempStack.pop();
				ArrayList<Integer> tempList = new ArrayList<Integer>(possibleValues.get(field));
				int otherNumberToSet = tempList.remove(0);
				field.setText(Integer.toString(otherNumberToSet));
				choices.put(field, tempList);
				order.push(field);
			}
		}
		if(!isCorrect()){			
			JOptionPane.showMessageDialog(frame, "Backtracking limit reached! ", "Error!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private boolean isFinished(){
		for(JTextField box : fields){
			if(box.getText().length() == 0){
				return false;
			}
		}
		return true;
	}
	
	private Boolean isCorrect(){
		if(!isFinished()){
			return false;
		}
		ArrayList<ArrayList<Integer>> sudoku = getSudoku();
		ArrayList<ArrayList<Integer>> transpose = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < 9; i++){
			transpose.add(new ArrayList<Integer>());
		}
		// Check row constraints
		for(ArrayList<Integer> row : sudoku){
			HashSet<Integer> rowSet = new HashSet<Integer>(row);
			if(rowSet.size() != row.size()){
				System.out.println("Mistake found in row: " + row.toString());
				return false;
			}
			for(int i = 0; i < 9; i++){
				transpose.get(i).add(row.get(i));
			}
		} // If still correct: check column constraints
		for(ArrayList<Integer> col : transpose){
			HashSet<Integer> colSet = new HashSet<Integer>(col);
			if(colSet.size() != col.size()){
				System.out.println("Mistake found in column: " + col.toString());
				return false;
			}
		} // If still correct: check block constraints
		for(int i = 0; i <= 2; i++){
			for(int j = 0; j <= 2; j++){
				ArrayList<Integer> block = new ArrayList<Integer>();
				for(int m = 0; m <= 2; m++){
					for(int n = 0; n <= 2; n++){
						ArrayList<Integer> row = sudoku.get(3*i+m);
						block.add(row.get(3*j+n));
					}
				}
				HashSet<Integer> blockSet = new HashSet<Integer>(block);
				if(blockSet.size() != block.size()){
					System.out.println("Mistake found in block: " + block.toString());
					return false;
				}
			}
		}
		return true;
	}

	
	private ArrayList<ArrayList<Integer>> getSudoku(){
		ArrayList<ArrayList<Integer>> sudoku = new ArrayList<ArrayList<Integer>>();
		for(int row = 0; row < 9; row++){
			ArrayList<Integer> rowList = new ArrayList<Integer>();
			for(int col = 0; col < 9; col++){
				int index = 9*row + col;
				rowList.add(Integer.parseInt(fields.get(index).getText()));
			}
			sudoku.add(rowList);
		}
		return sudoku;
	}
	
	private void loadSudokus(){
		try {
			FileInputStream is = new FileInputStream("sudokus");
			DataInputStream in = new DataInputStream(is);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			sudokus = new HashMap<Integer,ArrayList<ArrayList<Integer>>>();
			int amountOfLevels = choice.getItemCount();
			for(int i = 0; i < amountOfLevels; i++){
				int level = Integer.parseInt(choice.getItem(i));
				ArrayList<ArrayList<Integer>> temp = new ArrayList<ArrayList<Integer>>();
				sudokus.put(level,temp);
			}			
			String levelLine;
			while((levelLine = br.readLine()) != null){
				int level = Integer.parseInt(levelLine);
				ArrayList<Integer> sudoku = new ArrayList<Integer>();
				for(int i=1; i <= 9; i++){
					String line = br.readLine();
					String[] splittedLine = line.split(",");
					for(String temp : splittedLine){
						try {
							int number = Integer.parseInt(temp);
							if(number != 0){
								sudoku.add(number);
							} else {
								sudoku.add(0);
							}
						} catch (NumberFormatException e) {
							sudoku.add(0);
						}
					}
				}
				ArrayList<ArrayList<Integer>> levelSudokus = (ArrayList<ArrayList<Integer>>) sudokus.get(level);
				levelSudokus.add(sudoku);
				sudokus.put(level,levelSudokus);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
