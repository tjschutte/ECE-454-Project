package utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import main.Global;

public class JTextAreaOutputStream extends OutputStream
{
    private final JTextArea destination;
    private BufferedWriter writer;

    public JTextAreaOutputStream (JTextArea destination, boolean appendToLog)
    {
        if (destination == null)
            throw new IllegalArgumentException ("Destination is null");
        this.destination = destination;
        
        try {
			writer = new BufferedWriter(new FileWriter("log.txt", appendToLog));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException
    {
        final String text = new String (buffer, offset, length);
        SwingUtilities.invokeLater(new Runnable ()
            {
                @Override
                public void run() 
                {
                    try {
                    	destination.append(text);
                    	if (destination.getLineCount() > Global.SCREEN_LOG_SIZE) {
                    		destination.replaceRange("", 0, destination.getLineCount() - Global.SCREEN_LOG_SIZE);
                    	}
						writer.write(text);
						writer.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
                }
            });
    }

    @Override
    public void write(int b) throws IOException
    {
        write (new byte [] {(byte)b}, 0, 1);
    }
}
