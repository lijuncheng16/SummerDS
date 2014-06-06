package dshomework.processes;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.egit.github.core.User;

import dshomework.network.TransactionalFileOutputStream;




/**
 * GitHubDataPlugin is a DataPlugin for GitHub. Users are GitHub users, and
 * Posts are repositories.
 */
public class GitHubProcess extends MigratableProcess {
    /**
	 * 
	 */
	private String outputFile;
	private String username;
	private volatile boolean suspending = false;

	private TransactionalFileOutputStream outStream;
	private static final long serialVersionUID = 369940995850230662L;
	private final String NAME = "GitHub DataPlugin";
    static UserService service;
    public final static String gittoken = "45d262c43734f69d5c6f8005f3d0500b130bd2e4"; 

    /**
     * Authorizes the githubclient and services with the given token.
     */
    public GitHubProcess(String[] args) throws Exception {
    	if (args.length != 2) {
			System.out
			.println("usage: GitHubProcess <username> <friendlistoutputfile>");
			throw new Exception("Invalid Arguments");
		}
    	username = args[0];
		outputFile = args[1];
		
		
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(gittoken);
        service = new UserService(client);
        
        outStream = new TransactionalFileOutputStream(outputFile,false);
    }
    

    public List<String> getFollowersNames(String name) throws Exception {
        List<String> friends = new ArrayList<String>();
        try {
                 User user = service.getUser(name);
                 if (user == null) {
                	 System.out.println("No such user");
                	 }
            List<org.eclipse.egit.github.core.User> userList = service
                    .getFollowers(user.getName());
            for (org.eclipse.egit.github.core.User guser : userList)
                friends.add(guser.getLogin());
        } catch (IOException e) {
            throw new Exception("Couldn't get followers.", e);
        }
        return friends;
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		suspending = false;
		DataOutputStream out = new DataOutputStream(outStream);
		while (!suspending ) {
			try {
				List<String> friends =  getFollowersNames(username);
				for (String names: friends)
	                out.writeBytes(names);
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		suspending = false;
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		suspending = true;
		while (suspending );
	}

	@Override
	public void migrate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

}
