/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.driver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vikas_Singh
 */
@Repository
public class WhatsappRepository {
    
    HashMap<Group, List<User>> groupUserMap;
    HashMap<Group, List<Message>> groupMessageMap;
    HashMap<Group, User> adminMap;
    HashMap<Message, User> senderMap;
    HashSet<String> mobiles;
    int countGroup;
    int messageCount;

    public WhatsappRepository() {
        groupUserMap = new HashMap<>();
        groupMessageMap = new HashMap<>();
        adminMap = new HashMap<>();
        senderMap = new HashMap<>();
        mobiles = new HashSet<>();
        this.countGroup = 0;
        this.messageCount = 0;
    }

    public HashMap<Group, List<User>> getGroupUserMap() {
        return groupUserMap;
    }

    public void setGroupUserMap(HashMap<Group, List<User>> groupUserMap) {
        this.groupUserMap = groupUserMap;
    }

    public HashMap<Group, List<Message>> getGroupMessageMap() {
        return groupMessageMap;
    }

    public void setGroupMessageMap(HashMap<Group, List<Message>> groupMessageMap) {
        this.groupMessageMap = groupMessageMap;
    }

    public HashMap<Group, User> getAdminMap() {
        return adminMap;
    }

    public void setAdminMap(HashMap<Group, User> adminMap) {
        this.adminMap = adminMap;
    }

    public int getCountGroup() {
        return countGroup;
    }

    public void setCountGroup(int countGroup) {
        this.countGroup = countGroup;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }
    
    
    
    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        if(mobiles.contains(mobile)) {
            throw new Exception("User already exists");
        }
        User user = new User();
        user.setName(name);
        user.setMobile(mobile);
        mobiles.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.
         Group group = new Group();
        
         if(users.size()==2) {
              group.setName(users.get(1).getName());
          } else group.setName("Group "+(++countGroup));
         group.setNumberOfParticipants(users.size());
         groupUserMap.put(group, users);
         groupMessageMap.put(group, new ArrayList<>());
         adminMap.put(group,users.get(0));
         return group;
    }

    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        Message message = new Message();
        message.setId(++messageCount);
        message.setContent(content);
        message.setTimestamp(new Date());
        return message.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if(!groupUserMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        }
        boolean isMember = false;
        for(User user : groupUserMap.get(group)) {
            if(sender.equals(user)) {
                groupMessageMap.get(group).add(message);
                senderMap.put(message, sender);
                isMember = true;
                break;
            }
        }
        if(!isMember) throw new Exception("You are not allowed to send message");
        else return groupMessageMap.get(group).size();
    }
    
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.
        if(!adminMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        } 
        if(!adminMap.get(group).equals(approver)) throw new Exception("Approver does not have rights");
        boolean participant = false;
        for(User u : groupUserMap.get(group)) {
            if(u.equals(user)) {
                participant = true;
                break;
            }
        }
        if(!participant) throw new Exception("User is not a participant");
        adminMap.put(group, user);
        return "SUCCESS";
    }
  
    public int removeUser(User user) throws Exception{
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)

        for(Group group : groupUserMap.keySet()) {
            for(User u : groupUserMap.get(group)) {
                if(u.equals(user)) {
                    if(adminMap.get(group).equals(user)) {
                        throw new Exception("Cannot remove admin");
                    } else {
                        for(Message m : groupMessageMap.get(group)) {
                            if(senderMap.get(m).equals(u)) {
                                senderMap.remove(m);
                                groupMessageMap.get(group).remove(m);
                            }
                        }
                        mobiles.remove(u.getMobile());
                        groupUserMap.get(group).remove(u);
                        return groupUserMap.get(group).size();
                    }
                }
            }
        }
        throw new Exception("User not found");
    }
}
