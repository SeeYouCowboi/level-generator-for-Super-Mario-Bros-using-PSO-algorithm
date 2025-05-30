package dk.itu.mario.engine;

import java.awt.*;

import javax.swing.*;

public class PlayCustomized {

	public static void main(String[] args)
     {
		    	JFrame frame = new JFrame("Mario Experience Showcase");
		    	MarioComponent mario = new MarioComponent(640, 480,2);
				//CustomType: 0为randomLevel，1为遗传算法关卡，2为PSO算法关卡

		    	frame.setContentPane(mario);
		    	frame.setResizable(false);
		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		        frame.pack();

		        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		        frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);

		        frame.setVisible(true);

		        mario.start();   
	}	

}
