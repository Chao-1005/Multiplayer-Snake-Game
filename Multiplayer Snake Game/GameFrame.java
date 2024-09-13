import javax.swing.JFrame;

@SuppressWarnings("serial")
public class GameFrame extends JFrame{

	public GameFrame(){
		this.add(new GamePanel());
		this.setTitle("SnakeGame");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
	}
}