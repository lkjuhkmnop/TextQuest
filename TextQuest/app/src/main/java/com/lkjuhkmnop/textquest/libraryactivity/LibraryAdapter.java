package com.lkjuhkmnop.textquest.libraryactivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkjuhkmnop.textquest.R;
import com.lkjuhkmnop.textquest.tools.Tools;
import com.lkjuhkmnop.textquest.tqmanager.DBGame;
import com.lkjuhkmnop.textquest.tqmanager.DBQuest;

import java.util.Calendar;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {
    private Context context;
    private LibraryActivity libraryActivity;
    private DBQuest[] quests;

    public void setQuests(DBQuest[] quests) {
        this.quests = quests;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView questId, questTitle, questAuthor;
        ImageView questNewGame, questSettings, questDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            questId = itemView.findViewById(R.id.quest_id);
            questTitle = itemView.findViewById(R.id.lib_quest_title);
            questAuthor = itemView.findViewById(R.id.lib_quest_author);
            questNewGame = itemView.findViewById(R.id.lib_quest_new_game);
            questSettings = itemView.findViewById(R.id.lib_quest_settings);
            questDelete = itemView.findViewById(R.id.lib_quest_delete);
        }

        public void setIdText(String idText) {
            questId.setText(idText);
        }

        public void setTitleText(String title) {
            questTitle.setText(title);
        }

        public void setAuthorText(String author) {
            questAuthor.setText(author);
        }

        public View getItemView() {
            return itemView;
        }
    }

    public LibraryAdapter(Context context, LibraryActivity libraryActivity,  DBQuest[] quests) {
        this.context = context;
        this.libraryActivity = libraryActivity;
        this.quests = quests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lib_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Set quests description
        holder.setIdText(String.valueOf(quests[position].getQuestId()));
        holder.setTitleText(quests[position].getQuestTitle());
        holder.setAuthorText(quests[position].getQuestAuthor());

//        Set click listeners
//        For description
        holder.getItemView().findViewById(R.id.lib_description).setOnClickListener(v -> Toast.makeText(v.getContext(), quests[position].getQuestTitle(), Toast.LENGTH_SHORT).show());

//        For the new game button
        ImageView addButton = holder.getItemView().findViewById(R.id.lib_quest_new_game);
        addButton.setOnClickListener(v -> {
            String newGameTitle = quests[position].getQuestTitle();
            try {
                while (Tools.getTqManager().getGameByTitle(context, newGameTitle) != null) {
                    newGameTitle = newGameTitle + "_n";
                }
                DBGame newGame = new DBGame(quests[position].getQuestId(), newGameTitle, Calendar.getInstance().getTimeInMillis());
                Tools.getTqManager().addGame(context, newGame);
                Tools.startPlayActivity(libraryActivity, addButton, newGameTitle);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

//        For the settings button

//        For the delete button
        holder.getItemView().findViewById(R.id.lib_quest_delete).setOnClickListener(v -> {
            try {
                Tools.getTqManager().deleteQuestById(context, quests[position].getQuestId());
                LibraryActivity.reloadQuestsList();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return quests == null ? 0 : quests.length;
    }
}
