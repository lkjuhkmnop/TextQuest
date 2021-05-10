package com.lkjuhkmnop.textquest.tqmanager;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lkjuhkmnop.textquest.tools.Tools;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class CloudManager {
    public static final int OK = 1;
    public static final int NO_SUCH_DOCUMENT = 2;
    public static final int FAILED = 3;

    public static final int QUEST_MATCH = 4;
    public static final int NO_QUEST_MATCH = 5;

    private static final String FS_LIBRARY_COLLECTION = "tqlibrary";
    private static final String DBQ_UPLOADER_USER_ID_FIELD = "questUploaderUserId";
    private static final String DBQ_TITLE_FIELD = "questTitle";
    private static final String DBQ_AUTHOR_FIELD = "questAuthor";
    private static final String FS_USERS_COLLECTION = "users";
    private static final String FS_USER_DISPLAY_NAME_FIELD = "display_name";
    private static final String FS_USER_UPLOADED_QUESTS_FIELD = "uploaded_quests";

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public static class CMResponse<T> {
         private int responseCode;
         private T data;
         private Exception exception;

        public CMResponse(int responseCode, T data, Exception exception) {
            this.responseCode = responseCode;
            this.data = data;
            this.exception = exception;
        }

        public CMResponse(int responseCode, T data) {
            this.responseCode = responseCode;
            this.data = data;
        }

        public CMResponse(int responseCode) {
            this.responseCode = responseCode;
        }

        public CMResponse(int responseCode, Exception exception) {
            this.responseCode = responseCode;
            this.exception = exception;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public T getData() {
            return data;
        }

        public Exception getException() {
            return exception;
        }

        @Override
        public String toString() {
            return "CMResponse{" +
                    "responseCode=" + responseCode +
                    ", data=" + data +
                    ", exception=" + exception +
                    '}';
        }
    }

    public interface OnCMResponseListener<T> {
        void onCMResponse(CMResponse<T> response);
    }


    public void uploadQuest(Context context, int questId, OnCMResponseListener<String> cmResponseListener) throws InterruptedException {
        DBQuest quest = Tools.tqManager().getQuestById(context, questId);
        quest.setQuestUploaderUserId(Tools.authTools().getUser().getUid());
        firestore.collection(FS_LIBRARY_COLLECTION)
                .add(quest)
                .addOnSuccessListener(documentReference -> {
                    cmResponseListener.onCMResponse(new CMResponse<String>(OK, documentReference.getId()));
//                    Update local database
                    quest.setQuestCloudId(documentReference.getId());
                    try {
                        Tools.tqManager().updateQuest(context, quest);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    Update cloud information about user's uploads
                    firestore.collection(FS_USERS_COLLECTION).document(Tools.authTools().getUser().getUid()).update(FS_USER_UPLOADED_QUESTS_FIELD, FieldValue.arrayUnion(documentReference.getId()));
                });
    }


    public void matchQuest(DBQuest quest, OnCMResponseListener<Integer> onCMResponseListener) {
        firestore.collection(FS_LIBRARY_COLLECTION).document(quest.getQuestCloudId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot == null) {
                        onCMResponseListener.onCMResponse(new CMResponse<Integer>(NO_SUCH_DOCUMENT, NO_QUEST_MATCH));
                    } else {
                        String cloudUploaderUserId = documentSnapshot.getString(DBQ_UPLOADER_USER_ID_FIELD);
                        if (cloudUploaderUserId != null && cloudUploaderUserId.equals(quest.getQuestUploaderUserId())) {
                            onCMResponseListener.onCMResponse(new CMResponse<Integer>(OK, QUEST_MATCH));
                        } else {
                            onCMResponseListener.onCMResponse(new CMResponse<Integer>(NO_SUCH_DOCUMENT, NO_QUEST_MATCH));
                        }
                    }
                }
            }
        });
    }


    public void deleteQuest(Context context, DBQuest localQuest) throws InterruptedException {
        Log.d("LKJD", "CLOUD_MANAGER: deleteQuest: quest_id=" + localQuest.getQuestId() + "; cloud_id: " + localQuest.getQuestCloudId() + "  METHOD INVOKED");
        firestore.collection(FS_LIBRARY_COLLECTION).document(localQuest.getQuestCloudId()).delete();
        firestore.collection(FS_USERS_COLLECTION).document(Tools.authTools().getUser().getUid()).update(FS_USER_UPLOADED_QUESTS_FIELD, FieldValue.arrayRemove(localQuest.getQuestCloudId()));
        Log.d("LKJD", "CLOUD_MANAGER: deleteQuest: quest_id=" + localQuest.getQuestId() + "; cloud_id=" + localQuest.getQuestCloudId() + "  UPDATE LOCAL QUEST");
        Tools.tqManager().updateQuestCloudInfo(context, localQuest.getQuestId());
    }


    public void getUserDisplayName(String uid, OnCMResponseListener<String> onCMResponseListener) {
        getUser(uid, new OnCMResponseListener<DocumentSnapshot>() {
            @Override
            public void onCMResponse(CMResponse<DocumentSnapshot> response) {
                if (response.responseCode == OK) {
                    onCMResponseListener.onCMResponse(new CMResponse<String>(OK, response.data.getString(FS_USER_DISPLAY_NAME_FIELD)));
                }
            }
        });
    }

    public void getUser(String uid, OnCMResponseListener<DocumentSnapshot> onCMResponseListener) {
        firestore.collection(FS_USERS_COLLECTION).document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        onCMResponseListener.onCMResponse(new CMResponse<DocumentSnapshot>(OK, document));
                    } else {
                        onCMResponseListener.onCMResponse(new CMResponse<DocumentSnapshot>(NO_SUCH_DOCUMENT));
                        Log.d("LKJD", "No such document");
                    }
                } else {
                    onCMResponseListener.onCMResponse(new CMResponse<DocumentSnapshot>(FAILED, task.getException()));
                    Log.d("LKJD", "get failed with ", task.getException());
                }
            }
        });
    }


    public void checkUserInUsersCollection() {
        firestore.collection(FS_USERS_COLLECTION).document(Tools.authTools().getUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot =  task.getResult();
                    if (!documentSnapshot.exists()) {
                        Map<String, Object> data = new TreeMap<String, Object>();
                        data.put(FS_USER_DISPLAY_NAME_FIELD, Tools.authTools().getUser().getDisplayName());
                        data.put(FS_USER_UPLOADED_QUESTS_FIELD, new ArrayList<String>(0));
                        firestore.collection(FS_USERS_COLLECTION).document(Tools.authTools().getUser().getUid()).set(data);
                    }
                }
            }
        });
    }
}
