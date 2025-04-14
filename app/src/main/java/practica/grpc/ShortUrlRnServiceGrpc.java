package practica.grpc;

import io.grpc.stub.StreamObserver;
import practica.logic.*;
import practica.services.UserServices;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import shorturlrn.ShortUrlRnGrpc;
import shorturlrn.ShortUrlRnGrpc.*;
import shorturlrn.ShortUrlRnOuterClass.CreateUrlRequests;
import shorturlrn.ShortUrlRnOuterClass.ShortUrlResponses;
import shorturlrn.ShortUrlRnOuterClass.UserRequests;
import shorturlrn.ShortUrlRnOuterClass.UserUrlListResponses;

public class ShortUrlRnServiceGrpc extends ShortUrlRnGrpc.ShortUrlRnImplBase {

    private final MongoService mongoService = new MongoService();

    @Override
    public void getUrlsByUser(UserRequests request, StreamObserver<UserUrlListResponses> responseObserver) {
        try {
            String username = request.getUsername();
            List<ShortURL> urls = mongoService.getShortURLsByUsername(username);
            
            UserUrlListResponses.Builder responseBuilder = UserUrlListResponses.newBuilder();
            
            for (ShortURL shortURL : urls) {
                List<URLAccessLog> logs = mongoService.getAccessLogsByUrlId(shortURL.getId(), shortURL);
                shortURL.setAccessLogs(logs);
                
                responseBuilder.addUrls(convertToProtoShortURL(shortURL));
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void createShortUrlAPI(CreateUrlRequests request, StreamObserver<ShortUrlResponses> responseObserver) {
        try {
            String username = request.getUsername();
            String originalUrl = request.getOriginalUrl();

            if (!isValidURL(originalUrl)) {
                responseObserver.onError(new IllegalArgumentException("Invalid URL"));
                return;
            }

            long nextId = mongoService.getNextShortURLId();
            String encodedShort = encodeUrl(nextId);
            
            User user = UserServices.getInstance().findByUsername(username);
            if (user == null) {
                responseObserver.onError(new IllegalArgumentException("User not found"));
                return;
            }

            ShortURL url = new ShortURL();
            url.setId(nextId);
            url.setOriginalUrl(originalUrl);
            url.setShortUrl(encodedShort);
            url.setUser(user);

            URLAccessLog creationLog = new URLAccessLog();
            creationLog.setId(mongoService.getNextAccessLogId());
            creationLog.setAccessTime(LocalDateTime.now());
            creationLog.setIp("creation");
            creationLog.setUrlEntry(url);
            url.getAccessLogs().add(creationLog);

            mongoService.saveShortURL(url);
            mongoService.saveAccessLog(creationLog);

            String base64Preview = ScreenshotUtils.generateBase64Preview(originalUrl);
            
            ShortUrlResponses response = ShortUrlResponses.newBuilder()
                .setOriginalUrl(originalUrl)
                .setShortUrl("http://your-domain.com/url/" + encodedShort)
                .setCreatedAt(convertToTimestamp(LocalDateTime.now()))
                .setPreviewImageBase64(base64Preview)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Helper methods for conversion between Java objects and protobuf messages
    private shorturlrn.ShortUrlRnOuterClass.ShortURL convertToProtoShortURL(ShortURL shortURL) {
        shorturlrn.ShortUrlRnOuterClass.ShortURL.Builder builder = shorturlrn.ShortUrlRnOuterClass.ShortURL.newBuilder()
            .setId(shortURL.getId())
            .setShortUrl(shortURL.getShortUrl())
            .setOriginalUrl(shortURL.getOriginalUrl())
            .setUser(convertToProtoUser(shortURL.getUser()))
            .setAccessCount(shortURL.getAccessCount());

        for (URLAccessLog log : shortURL.getAccessLogs()) {
            builder.addAccessLogs(convertToProtoUrlAccessLog(log));
        }

        return builder.build();
    }

    private shorturlrn.ShortUrlRnOuterClass.User convertToProtoUser(User user) {
        return shorturlrn.ShortUrlRnOuterClass.User.newBuilder()
            .setUsername(user.getUsername())
            .setName(user.getName())
            .setAdmin(user.isAdmin())
            .setAuthor(user.isAuthor())
            .setProfilePicture(shorturlrn.ShortUrlRnOuterClass.Image.newBuilder()
                .setId(user.getProfilePicture().getId())
                .setName(user.getProfilePicture().getName())
                .setMimeType(user.getProfilePicture().getMimeType())
                .setFotoBase64(user.getProfilePicture().getFotoBase64())
                .build())
            .build();
    }

    private shorturlrn.ShortUrlRnOuterClass.UrlAccessLog convertToProtoUrlAccessLog(URLAccessLog log) {
        return shorturlrn.ShortUrlRnOuterClass.UrlAccessLog.newBuilder()
            .setId(log.getId())
            .setIp(log.getIp())
            .setBrowser(log.getBrowser())
            .setClientDomain(log.getClientDomain())
            .setOs(log.getOs())
            .setAccessTime(convertToTimestamp(log.getAccessTime()))
            .build();
    }

    private Timestamp convertToTimestamp(LocalDateTime localDateTime) {
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }

    // Utility methods from your original code
    private boolean isValidURL(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String encodeUrl(long num) {
        final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.insert(0, BASE62.charAt((int) (num % 62)));
            num /= 62;
        }
        return sb.toString();
    }
}