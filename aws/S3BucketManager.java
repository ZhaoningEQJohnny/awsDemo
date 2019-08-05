package aws;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class S3BucketManager {
    public static void main(String[] args) {
        S3BucketManager  bucketManager = new S3BucketManager();
        String bucketName = "user-test-buck2";
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion(Regions.US_WEST_2)
                .build();
        //Bucket newBuck =bucketManager.createBucket(s3Client,bucketName);
        bucketManager.createFolder(s3Client,"datalink-sftp-demobuck","demoFol");

    }
    private  String s3Directory;
    public void createFolder(AmazonS3 s3Client,String bucketName, String folderName)
    {

        // create meta-data for your folder and set content-length to 0
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        // create empty content
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        // create a PutObjectRequest passing the folder name suffixed by /
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                folderName + "/", emptyContent, metadata);
        // send request to S3 to create folder
        s3Client.putObject(putObjectRequest);
        setDirectory(bucketName,folderName);

    }

    public void setDirectory(String bucketName, String folderName)
    {
        s3Directory = bucketName+"/"+folderName+"/";
    }
    public Bucket createBucket(AmazonS3 s3Client, String bucketName) {
        try {
            Bucket newBucket =null;
            if (!s3Client.doesBucketExistV2(bucketName)) {
                // Because the CreateBucketRequest object doesn't specify a region, the
                // bucket is created in the region specified in the client.
                newBucket = s3Client.createBucket(new CreateBucketRequest(bucketName));
                // Verify that the bucket was created by retrieving it and checking its location.
                String bucketLocation = s3Client.getBucketLocation(new GetBucketLocationRequest(bucketName));
                System.out.println("Bucket location: " + bucketLocation);
            } else {
                System.out.format("Bucket %s already exists.\n", bucketName);
               // newBucket = getBucket(bucketName);
            }
            return newBucket;
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Bucket getBucket(String bucket_name) {
        final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        Bucket named_bucket = null;
        List<Bucket> buckets = s3.listBuckets();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucket_name)) {
                named_bucket = b;
            }
        }
        return named_bucket;
    }

    public void grantBucketAccessControll(AmazonS3 s3Client, String bucketName) {

        ArrayList<Grant> grantCollection = new ArrayList<Grant>();

        // Grant the account owner full control.
        Grant grant1 = new Grant(new CanonicalGrantee(s3Client.getS3AccountOwner().getId()), Permission.FullControl);
        grantCollection.add(grant1);

        // Grant the LogDelivery group permission to write to the bucket.
        Grant grant2 = new Grant(GroupGrantee.LogDelivery, Permission.Write);
        grantCollection.add(grant2);

        // Save grants by replacing all current ACL grants with the two we just created.
        AccessControlList bucketAcl = new AccessControlList();
        bucketAcl.setOwner(s3Client.getS3AccountOwner());
        bucketAcl.grantAllPermissions(grantCollection.toArray(new Grant[0]));
        s3Client.setBucketAcl(bucketName, bucketAcl);

        // Retrieve the bucket's ACL, add another grant, and then save the new ACL.
//        AccessControlList newBucketAcl = s3Client.getBucketAcl(bucketName);
//        Grant grant3 = new Grant(new EmailAddressGrantee("johnnyqzn@gmail.com"), Permission.Read);
//        newBucketAcl.grantAllPermissions(grant3);
//        s3Client.setBucketAcl(bucketName, newBucketAcl);
    }
    public boolean findObject(AmazonS3 s3Client, String bucketName, String fileName)
    {
        return s3Client.doesObjectExist(bucketName,fileName);
    }


}
