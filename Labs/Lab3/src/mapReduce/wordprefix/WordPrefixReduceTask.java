package mapReduce.wordprefix;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mapReduce.frameWork.Emitter;
import mapReduce.frameWork.ReduceTask;

/**
 * The reduce task for a word-prefix map/reduce computation.
 * 
 * For each distinct header in a corpus of data, this reduce task will sum the
 * number of occurrences of each word having this header, and will pick the word
 * that has the highest occurrences, and emit a final key/value pair (the
 * key being the header, and the value being the that word).
 */
public class WordPrefixReduceTask implements ReduceTask {
    private static final long serialVersionUID = 6763871961687287020L;

    @Override
    public void execute(String key, Iterator<String> values, Emitter emitter) throws IOException {
    	Map<String, Integer> wordCountMap = new HashMap<String, Integer>();
    	int max = 0;
    	String maxCountWord = null;
    	
    	while(values.hasNext()) {
    		String word = values.next();
    		int count;
    		if (wordCountMap.containsKey(word)) {
    			count = wordCountMap.get(word);
    			wordCountMap.put(word, ++count);
    		} else {
    			wordCountMap.put(word, 1);
    			count = 1;
    		}
    		if (count > max) {
				max = count;
				maxCountWord = word;
			}
    	}
    	
    	emitter.emit(key, maxCountWord);
    }

}

