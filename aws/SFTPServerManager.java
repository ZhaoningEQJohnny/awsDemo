package aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.transfer.AWSTransferClient;
import com.amazonaws.services.transfer.AWSTransferClientBuilder;
import com.amazonaws.services.transfer.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SFTPServerManager {
    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public SFTPServerManager() {
        this.tags = new ArrayList<>();
    }

    private List<Tag> tags;

    public static void main(String[] args) {
        String endpointType = EndpointType.PUBLIC.toString();
        String identityProviderType = IdentityProviderType.SERVICE_MANAGED.toString();
        String loggingRole = "";
        SFTPServerManager serverManager =  new SFTPServerManager();
        String serverID = serverManager.createServer(serverManager.getAWSTransferClient(serverManager.getCredential()),
                endpointType,identityProviderType,"",serverManager.getTags());
        System.out.println(serverID);
    }



    public void setTags(HashMap<String, String> tagMap) {
        for (Map.Entry<String, String> entry : tagMap.entrySet()) {
            tags.add(new Tag().withKey(entry.getKey()).withValue(entry.getValue()));
        }
    }

    public String createServer(AWSTransferClient transferClient, String endpointType, String identityProviderType,
                             String loggingRole, List<Tag> tags) {
        CreateServerRequest createServerRequest = new CreateServerRequest();
        createServerRequest.setEndpointType(endpointType);
        createServerRequest.setIdentityProviderType(identityProviderType);
        if (loggingRole.length() != 0)
            createServerRequest.setLoggingRole(loggingRole);
        if (!tags.isEmpty())
            createServerRequest.setTags(tags);
        CreateServerResult serverResult =  transferClient.createServer(createServerRequest);
        return  serverResult.getServerId();


    }


    public ProfileCredentialsProvider getCredential() {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        return credentialsProvider;

    }

    public AWSTransferClient getAWSTransferClient(ProfileCredentialsProvider credentialsProvider) {
        AWSTransferClientBuilder builder = AWSTransferClientBuilder.standard().withCredentials(credentialsProvider)
                .withClientConfiguration(new ClientConfiguration()).withRegion(Regions.US_WEST_2);

        AWSTransferClient transferClient = (AWSTransferClient) builder.build();
        return transferClient;
    }

}
