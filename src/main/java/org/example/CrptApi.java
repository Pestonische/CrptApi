package org.example;
import java.io.IOException;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.gson.*;
import okhttp3.*;

import java.util.concurrent.*;

public class CrptApi
{
    @Data
    @NoArgsConstructor
    private static class Document{
        private String discription;
        private String participantInn;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private Date production_date;
        private String production_type;
        private Product products;
        private Date reg_date;
        private String reg_number;
    }
    @Data
    @NoArgsConstructor
    private static class Product{
        private String certificate_document;
        private Date certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private Date production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
    }
    private final Semaphore sem;
    private static final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final Gson gson = new Gson();
    public CrptApi(TimeUnit timeUnit, int requestLimit){
        sem = new Semaphore(requestLimit);
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(requestLimit);
        executorService.scheduleAtFixedRate(
                sem::release,
                0L,
                timeUnit.toMillis(1),
                TimeUnit.MILLISECONDS
        );
    }
    public String creatingDocument( Document document, String signature) throws InterruptedException, IOException {
        sem.acquire();

        String jsonDoc = gson.toJson(document);

        OkHttpClient client = new OkHttpClient();


        RequestBody requestbody = RequestBody.create(
                MediaType.parse("application/json"), jsonDoc);

        Request request = new Request.Builder()
                .url(URL)
                .post(requestbody)
                .addHeader("Signature", signature)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException(String.valueOf(response));
            }
            return response.body().string();
        } catch (IOException e) {
            return e.toString();
        }

    }


    public static void main( String[] args ) throws IOException, InterruptedException {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 5);
        for(int i = 0; i<10; i++) {
            String result = crptApi.creatingDocument(new Document(), "signature");
            System.out.println(result);
        }
    }
}
