package com.example.kotlinmessenger.messages

import android.R.id.message
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*


class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra(NewMessageActivity.USER_KEY)
        Log.d("chat", "$toUser")

        supportActionBar?.title = toUser?.username

        listenForMessages()

        send_button_chat_log.setOnClickListener {
            Log.d(TAG, "attemp to send messsage")
            performSendMessage()
        }

    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                val currentUser = LatestMessagesActivity.currentUser ?: return
                if(chatMessage?.fromId == FirebaseAuth.getInstance().uid) {
                    adapter.add(ChatToItem(chatMessage?.text.toString(), currentUser))
                } else {
                    adapter.add(ChatFromItem(chatMessage?.text.toString(), toUser!!))
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
        })

        recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
    }

    class ChatMessage(
        val id: String, val text: String, val fromId: String, val toId: String,
        val timeStamp: Long
    ) {
    constructor() : this("", "", "", "", -1)
    }

    private fun performSendMessage() {
        // how to send a message to firebaseDatabase
//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val text = edittext_chag_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        val chatMessage = ChatMessage(reference.key!!, text, fromId!!, toId!!, System.currentTimeMillis())
        reference.setValue(chatMessage).addOnSuccessListener {
            edittext_chag_log.text.clear()
            recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
        }
        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$fromId/$toId")
        val toLatestMessageRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$toId/$fromId")

        latestMessageRef.setValue(chatMessage)
        toLatestMessageRef.setValue(chatMessage)
    }
}


class ChatFromItem(val text: String, val user : User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.chat_from_row.text = text
        val targetImageView = viewHolder.itemView.profile_chat_from_row
        val uri = user.profileImageUrl
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user : User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.chat_to_row.text = text
        val targetImageView = viewHolder.itemView.profile_chat_to_row
        val uri = user.profileImageUrl
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}

