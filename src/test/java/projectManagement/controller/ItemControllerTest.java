package projectManagement.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import projectManagement.controller.entities.CreateItem;
import projectManagement.entities.*;
import projectManagement.repository.ItemRepo;
import projectManagement.service.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {

    @InjectMocks
    private ItemController itemController;

    @Mock
    ItemRepo itemRepo;
    @Mock
    BoardService boardService;
    @Mock
    ItemService itemService;
    @Mock
    AuthService authService;
    @Mock
    SocketsUtil socketsUtil;
    @Mock
    NotificationService notificationService;
    private Board board;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        board = new Board();
    }

    @Test
    public void createItem_validInput_returnsSuccessResponse() {

        String title = "new item";
        String status = "new status";

        CreateItem item = new CreateItem(status, title);

        given(boardService.statusExistsInBoard(board, status)).willReturn(Response.createSuccessfulResponse(status));
        Response<Item> successfulResponse = Response.createSuccessfulResponse(new Item(title, status, board, user));
        given(itemService.createItem(title, status, user, board)).willReturn(successfulResponse);
        ResponseEntity<String> item1 = itemController.createItem(board, user, item);
        verify(socketsUtil).createItem(successfulResponse, board.getId());


        assertEquals(HttpStatus.OK, item1.getStatusCode());


    }

    @Test
    public void createItem_wrongInput_returnsBadRequestResponse() {
        board = new Board();
        String title = "new item";
        String status = "new status";
        CreateItem item = new CreateItem(status, title);

        given(boardService.statusExistsInBoard(board, status)).willReturn(Response.createFailureResponse(status));
        ResponseEntity<String> item1 = itemController.createItem(board, user, item);

        assertEquals(HttpStatus.BAD_REQUEST, item1.getStatusCode());

    }

    @Test
    public void changeType_withValidType_returnSuccessResponse() {

        String title = "new item";
        String status = "new status";
        String type = "new type";

        Item oldItem = new Item(title, status, board, user);
        Item newItem = new Item(title, status, board, user);
        newItem.setType(type);


        given(boardService.typeExistsInBoard(board, type)).willReturn(Response.createSuccessfulResponse("ok"));
        Response<Item> successfulResponse = Response.createSuccessfulResponse(newItem);
        given(itemService.changeType(oldItem.getId(), type, board)).willReturn(successfulResponse);
        ResponseEntity<String> resItem = itemController.changeType(board, oldItem.getId(), type);

        verify(socketsUtil).updateItem(successfulResponse.getData(), board.getId());


        assertEquals(HttpStatus.OK, resItem.getStatusCode());

    }

    @Test
    public void changeType_withInValidType_returnBadRequestResponse() {
        long itemId = 2;
        String type = "new type";
        given(boardService.typeExistsInBoard(board, type)).willReturn(Response.createFailureResponse(null));
        ResponseEntity<String> changeTypeResponse = itemController.changeType(board, itemId, type);
        assertEquals(HttpStatus.BAD_REQUEST, changeTypeResponse.getStatusCode());


    }

    @Test
    public void changeStatus_withValidStatus_returnSuccessResponse() {

        String title = "new item";
        String oldStatus = "old status";
        String newStatus = "new status";

        Item oldItem = new Item(title, oldStatus, board, user);
        Item newItem = new Item(title, oldStatus, board, user);
        newItem.setStatus(newStatus);


        given(boardService.statusExistsInBoard(board, newStatus)).willReturn(Response.createSuccessfulResponse("ok"));
        Response<Item> successfulResponse = Response.createSuccessfulResponse(newItem);
        given(itemService.changeStatus(oldItem.getId(), newStatus, board)).willReturn(successfulResponse);
        Set<Long> allUserInBoard = Stream.of(1L, 2L, 3L).collect(Collectors.toCollection(HashSet::new));
        given(boardService.getAllUsersInBoardByBoardId(board.getId())).willReturn(allUserInBoard);
        ResponseEntity<String> resItem = itemController.changeStatus(board, oldItem.getId(), newStatus);

        verify(socketsUtil).updateItem(successfulResponse.getData(), board.getId());
        verify(notificationService).sendNotification(allUserInBoard, "the status is changed in itemnew item new status is new status", NotifyWhen.ITEM_STATUS_CHANGED);


        assertEquals(HttpStatus.OK, resItem.getStatusCode());
        assertEquals(newItem, successfulResponse.getData());


    }

    @Test
    public void changeStatus_withInValidStatus_returnBadRequestResponse() {
        long itemId = 2;
        String status = "new status";
        given(boardService.statusExistsInBoard(board, status)).willReturn(Response.createFailureResponse(null));
        ResponseEntity<String> changeStatusResponse = itemController.changeStatus(board, itemId, status);
        assertEquals(HttpStatus.BAD_REQUEST, changeStatusResponse.getStatusCode());


    }

    @Test
    public void changeItemDescription_withValidItemId_returnSuccessResponse() {
        String title = "new item";
        String status = "new status";
        String newDescription = "new description";

        Item oldItem = new Item(title, status, board, user);
        Item newItem = new Item(title, status, board, user);
        newItem.setDescription(newDescription);

        Response<Item> successfulResponse = Response.createSuccessfulResponse(newItem);
        given(itemService.changeDescription(oldItem.getId(), newDescription, board)).willReturn(successfulResponse);
        ResponseEntity<String> resItem = itemController.changeItemDescription(board, oldItem.getId(), newDescription);

        verify(socketsUtil).updateItem(successfulResponse.getData(), board.getId());


        assertEquals(HttpStatus.OK, resItem.getStatusCode());
        assertEquals(newItem, successfulResponse.getData());
    }

    @Test
    public void changeItemDescription_withInValidItemId_returnBadRequestResponse() {
        long itemId = 2;
        String description = "new description";
        given(itemService.changeDescription(itemId, description, board)).willReturn(Response.createFailureResponse(null));
        ResponseEntity<String> changeDescriptionResponse = itemController.changeItemDescription(board, itemId, description);
        assertEquals(HttpStatus.BAD_REQUEST, changeDescriptionResponse.getStatusCode());


    }

    @Test
    public void changeAssignToUser_withValidUserId_returnSuccessResponse() {
        String title = "new item";
        String status = "new status";
        User assignedToUser = new User();

        Item oldItem = new Item(title, status, board, user);
        Item newItem = new Item(title, status, board, user);
        newItem.setAssignedToUser(assignedToUser);

        given(authService.getUser(assignedToUser.getId())).willReturn(Optional.of(assignedToUser));
        given(boardService.userExistsInBoard(board, assignedToUser)).willReturn(Response.createSuccessfulResponse(UserRole.USER));
        Response<Item> successfulResponse = Response.createSuccessfulResponse(newItem);
        given(itemService.changeAssignedToUser(oldItem.getId(), assignedToUser, board)).willReturn(successfulResponse);;
       ResponseEntity<Response<Item>> resItem = itemController.changeAssignToUser(board, oldItem.getId(), assignedToUser.getId());

        verify(socketsUtil).updateItem(successfulResponse.getData(), board.getId());


        assertEquals(HttpStatus.OK, resItem.getStatusCode());
        assertEquals(newItem, successfulResponse.getData());


    }

    @Test
    public void changeAssignToUser_withInValidUserId_returnBadRequestResponse() {
        long itemId = 2;
        long userId = 10;
        given(authService.getUser(userId)).willReturn(Optional.empty());
        ResponseEntity<Response<Item>> changeAssignToUserResponse = itemController.changeAssignToUser(board, itemId, userId);
        assertEquals(HttpStatus.BAD_REQUEST, changeAssignToUserResponse.getStatusCode());


    }

    @Test
    public void addComment_withValidItemId_returnSuccessResponse() {
        String title = "new item";
        String status = "new status";
        String newComment = "new comment";

        Item item = new Item(title, status, board, user);
        Comment comment = new Comment(newComment, user, item, LocalDateTime.now());
        Item newItem = new Item(title, status, board, user);
        Set<Comment> allComments = Stream.of(comment).collect(Collectors.toCollection(HashSet::new));
        newItem.setComments(allComments);

        Response<Item> successfulResponse = Response.createSuccessfulResponse(newItem);
        given(itemService.addComment(item.getId(), board, user, newComment)).willReturn(successfulResponse);


        Set<Long> allUserInBoard = Stream.of(1L, 2L, 3L).collect(Collectors.toCollection(HashSet::new));
        given(boardService.getAllUsersInBoardByBoardId(board.getId())).willReturn(allUserInBoard);


        ResponseEntity<String> resItem = itemController.addComment(user, board, item.getId(), newComment);


        verify(socketsUtil).updateItem(successfulResponse.getData(), board.getId());
        verify(notificationService).sendNotification(allUserInBoard, "add commentnew item new comment is added new comment", NotifyWhen.ITEM_COMMENT_ADDED);


        assertEquals(HttpStatus.OK, resItem.getStatusCode());
        assertEquals(newItem, successfulResponse.getData());


    }

    @Test
    public void addComment_withInValidItemId_returnBadRequestResponse() {
        long itemId = 2;
        String newComment = "new comment";
        Response<Item> failureResponse = Response.createFailureResponse(null);
        given(itemService.addComment(itemId, board, user, newComment)).willReturn(failureResponse);

        ResponseEntity<String> responseAddComment = itemController.addComment(user, board, itemId, newComment);
        assertEquals(HttpStatus.BAD_REQUEST, responseAddComment.getStatusCode());


    }

    @Test
    public void deleteComment_withValidCommentId_returnSuccessResponse() {
        long commentId = 2;
        Item item = new Item("new item", "new status", board, user);
        Response<Item> successfulResponse = Response.createSuccessfulResponse(item);
        given(itemService.deleteComment(board, user, commentId)).willReturn(successfulResponse);


        ResponseEntity<String> resItem = itemController.deleteComment(user, board, commentId);


        verify(socketsUtil).updateItem(successfulResponse.getData(), board.getId());


        assertEquals(HttpStatus.OK, resItem.getStatusCode());
        assertEquals(item, successfulResponse.getData());


    }

    @Test
    public void deleteComment_withInValidCommentId_returnBadRequestResponse() {
        long commentId = 2;

        Response<Item> failureResponse = Response.createFailureResponse(null);
        given(itemService.deleteComment(board, user, commentId)).willReturn(failureResponse);

        ResponseEntity<String> responseDeleteComment = itemController.deleteComment(user, board, commentId);
        assertEquals(HttpStatus.BAD_REQUEST, responseDeleteComment.getStatusCode());


    }


    @Test
    public void updateItemImportance_withValidItemId_returnSuccessResponse() {

        Item item = new Item("new item", "new status", board, user);
        Item newItem = new Item("new item2", "new status", board, user);
        newItem.setImportance(ItemImportance.ONE)
        ;
        Response<Item> successfulResponse = Response.createSuccessfulResponse(newItem);
        given(itemService.updateImportance(board , user , item.getId() , ItemImportance.ONE)).willReturn(successfulResponse);


        ResponseEntity<String> res = itemController.updateItemImportance(user, board, item.getId(), ItemImportance.ONE);


        verify(socketsUtil).updateItem(successfulResponse.getData(), board.getId());


        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(newItem, successfulResponse.getData());


    }

    @Test
    public void updateImportance_withInValidItemId_returnBadRequestResponse() {
        long commentId = 2;

        Response<Item> failureResponse = Response.createFailureResponse(null);
        given(itemService.deleteComment(board, user, commentId)).willReturn(failureResponse);

        ResponseEntity<String> responseDeleteComment = itemController.deleteComment(user, board, commentId);
        assertEquals(HttpStatus.BAD_REQUEST, responseDeleteComment.getStatusCode());


    }

    @Test
    public void createSubItem_withParentId_returnSuccessResponse() {
        String childTitle = "chile item";
        Item item = new Item("new item", "new status", board, user);
        Item newItem = new Item(childTitle, "new status", board, user);
//        newItem.setImportance(ItemImportance.ONE)

        Response<Item> successfulResponse = Response.createSuccessfulResponse(newItem);
        given(itemService.createSubItem(childTitle , user , board , item.getId())).willReturn(successfulResponse);


        ResponseEntity<String> res = itemController.createSubItem(user , board , item.getId() , childTitle);


        verify(socketsUtil).createItem(successfulResponse, board.getId());


        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(newItem, successfulResponse.getData());


    }

    @Test
    public void createSubItem_withInValidParentId_returnBadRequestResponse() {
        long parentId = 2;

        Response<Item> failureResponse = Response.createFailureResponse(null);
        given(itemService.createSubItem("no title",user ,board, parentId)).willReturn(failureResponse);

        ResponseEntity<String> responseDeleteComment = itemController.createSubItem(user, board,parentId , "no title");
        assertEquals(HttpStatus.BAD_REQUEST, responseDeleteComment.getStatusCode());


    }

}
