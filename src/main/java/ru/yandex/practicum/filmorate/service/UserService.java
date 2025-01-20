package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(long userId, long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());
    }

    public void deleteFriend(long userId, long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());
    }

    public List<User> commonFriends(long userId, long friendId) {
        Set<Long> userFriends = userStorage.findById(userId).getFriends();
        Set<Long> friendList = userStorage.findById(friendId).getFriends();
        List<Long> commonFriends = userFriends.stream().filter(friendList::contains).toList();
        List<User> commonList = new ArrayList<>();
        if (!commonFriends.isEmpty()) {
            for (Long commonFriend : commonFriends) {
                commonList.add(userStorage.findById(commonFriend));
            }
        }
        return commonList;
    }

    public List<User> userFriends(long userId) {
        Set<Long> userFriends = userStorage.findById(userId).getFriends();
        ;

        List<User> friendsList = new ArrayList<>();
        if (!userFriends.isEmpty()) {
            for (Long friendId : userFriends) {
                friendsList.add(userStorage.findById(friendId));
            }
        }
        return friendsList;
    }

}
