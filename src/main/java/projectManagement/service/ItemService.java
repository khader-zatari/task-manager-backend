package projectManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectManagement.controller.entities.*;
import projectManagement.entities.*;
import projectManagement.repository.CommentRepo;
import projectManagement.repository.ItemRepo;
//import projectManagement.repository.StatusRepo;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    @Autowired
    ItemRepo itemRepo;
    @Autowired
    CommentRepo commentRepo;
//    @Autowired
//    StatusRepo statusRepo;


    public Response<Item> createItem(String title, String status, User creator, Board board){
        return Response.createSuccessfulResponse(itemRepo.save(new Item(title, status, board, creator)));
    }


    public Response<Item> deleteItem(long itemId) {
        Optional<Item> itemFound = itemRepo.findById(itemId);

        if (!itemFound.isPresent()) {
            return Response.createFailureResponse("Item doesn't exist");
        }

        itemRepo.deleteById(itemId);
        return Response.createSuccessfulResponse(itemFound.get());
    }

    public Response<Item> changeType(long itemId, String type) {
        Optional<Item> optItem = itemRepo.findById(itemId);
        if(!optItem.isPresent()){
            return Response.createFailureResponse("Item does not exist");
        }
        Item item = optItem.get();
        item.setType(type);
        itemRepo.save(item);

        //todo: add live update
        return Response.createSuccessfulResponse(item);

    }

    public Response<Item> changeStatus(long itemId, String status){
        Optional<Item> optItem = itemRepo.findById(itemId);

        if(!optItem.isPresent()){
            return Response.createFailureResponse("Item does not exist");
        }
        Item item = optItem.get();
        item.setStatus(status);
        itemRepo.save(item);

        //todo: add live update
        return Response.createSuccessfulResponse(item);
    }

    public Response<Item> changeDescription(long itemId, String description) {
        Optional<Item> item = itemRepo.findById(itemId);
        if(!item.isPresent()){
            return Response.createFailureResponse("Can not update description- item does not exist");
        }

        Item updateItem = item.get();
        updateItem.setDescription(description);

        return Response.createSuccessfulResponse(itemRepo.save(updateItem));
    }


    public Response<Item> changeAssignedToUser(Long itemId, User assignedToUser) {
        Optional<Item> itemFound = itemRepo.findById(itemId);
        if (!itemFound.isPresent()) {
            return Response.createFailureResponse("the item doesn't exist");
        }
        Item item = itemFound.get();


        item.setAssignedToUser(assignedToUser);

        Item savedItem = itemRepo.save(item);


        return Response.createSuccessfulResponse(savedItem);
    }

    public Optional<Item> getItem(long itemId) {
        return itemRepo.findById(itemId);
    }

    public Response<List<Item>> getAll() {
        return Response.createSuccessfulResponse(itemRepo.findAll());
    }

    public Response<List<Item>> getBoardItems(Long boardId) {
        return Response.createSuccessfulResponse(itemRepo.findByBoardId(boardId));
    }


    /**
     * This method filters items of the given board by given properties and their values.
     * It creates ItemSpecification object
     *
     * @param filter  - properties and their values we want to filter by
     * @param boardId
     * @return Response containing the list of items that match all of the given properties
     */
    public Response<List<Item>> filterItems(FilterItemDTO filter, Long boardId) {
        ItemSpecification specification = new ItemSpecification(filter, boardId);
        return Response.createSuccessfulResponse(itemRepo.findAll(specification));
    }

//    public Response<Comment> addComment(Item item, User user, String comment) {
//        Comment commentObj = new Comment(comment, user, item, LocalDateTime.now());
//        Comment savedComment = commentRepo.save(commentObj);
//
//
//        return Response.createSuccessfulResponse(savedComment);
//    }

    public Response<Long> deleteComment(Long commentId) {
        Optional<Comment> commentFound = commentRepo.findById(commentId);

        if (!commentFound.isPresent()) {
            return Response.createFailureResponse("the comment doesn't exist");
        }

        commentRepo.deleteById(commentId);
        return Response.createSuccessfulResponse(commentId);
    }

    public Response<Item> changeAssignedUser(long itemId, User user){
        Optional<Item> optItem = itemRepo.findById(itemId);
        if(!optItem.isPresent()){
            return Response.createFailureResponse("Can not change assigned user - item does not exist");
        }

        Item item = optItem.get();
        item.setAssignedToUser(user);
        itemRepo.save(item);
        //todo: add live update

        return Response.createSuccessfulResponse(item);
    }

    public Response<Item> addComment(long itemId, long boardId, User user,  String commentStr){
        Optional<Item> item = itemRepo.findById(itemId);
        if(!item.isPresent()){
            return Response.createFailureResponse("Item does not exist");
        }

        if(item.get().getBoard().getId() != boardId){
            return Response.createFailureResponse("Item does not exist in board");
        }

        Comment comment = new Comment(commentStr, user, item.get(), LocalDateTime.now());
        commentRepo.save(comment);

        return Response.createSuccessfulResponse(item.get());

    }

    public Response<Item> deleteComment(long boardId, User user, long commentId){
        Optional<Comment> comment = commentRepo.findById(commentId);
        if(!comment.isPresent()){
            return Response.createFailureResponse("Comment does not exist");
        }

        if(comment.get().getItem().getBoard().getId() != boardId){
            return Response.createFailureResponse("Comment does not exist in board");
        }

        long itemId = comment.get().getItem().getId();
        commentRepo.delete(comment.get());

        return Response.createSuccessfulResponse(itemRepo.getReferenceById(itemId));

    }




}
