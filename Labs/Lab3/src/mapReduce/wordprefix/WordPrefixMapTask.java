package mapReduce.wordprefix;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import mapReduce.frameWork.Emitter;
import mapReduce.frameWork.MapTask;

/**
 * The map task for a word-prefix map/reduce computation.
 * 
 * For each occurrence of a word in a corpus of data, this map task will emit a
 * sequence of key/value pair to a file on the disk, which conforms to the key
 * extraction rule explained in piazza, to be later accessed during the reduce
 * portion of the computation.
 */
public class WordPrefixMapTask implements MapTask {
    private static final long serialVersionUID = 3046495241158633404L;

    @Override
    public void execute(InputStream in, Emitter emitter) throws IOException {
    	Scanner scanner = new Scanner(in);
    	scanner.useDelimiter("\\W+");
    	while (scanner.hasNext()) {
    		String word = scanner.next().trim().toLowerCase();
    		if (word.length() > 1) {
	    		for (int i = 1; i <= word.length(); i++) {
	    			String key = word.substring(0, i);
	    			emitter.emit(key, word);
	    		}
    		}
    		emitter.emit(word, word);
    	}
    	scanner.close();
    }

}
