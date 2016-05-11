package com.beppeben.cook4server.service;

import com.beppeben.cook4server.domain.C4AuthRequest;
import com.beppeben.cook4server.domain.C4Dish;
import com.beppeben.cook4server.domain.C4DishComment;
import com.beppeben.cook4server.domain.C4Image;
import com.beppeben.cook4server.domain.C4Item;
import com.beppeben.cook4server.domain.C4Query;
import com.beppeben.cook4server.domain.C4Rating;
import com.beppeben.cook4server.domain.C4Report;
import com.beppeben.cook4server.domain.C4SignUpRequest;
import com.beppeben.cook4server.domain.C4SwapProposal;
import com.beppeben.cook4server.domain.C4Tag;
import com.beppeben.cook4server.domain.C4Transaction;
import com.beppeben.cook4server.domain.C4User;
import com.beppeben.cook4server.domain.C4UserComment;
import com.beppeben.cook4server.utils.Configs;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("/cook4")
@ApplicationScoped
public class ApiMap implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger
            .getLogger(ApiMap.class.getName());

    @EJB
    Services service;

    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public C4User register(@Valid C4User user, @HeaderParam("Token") String token) {
        return service.register(user, token);
    }

    @POST
    @Path("/dish/cover={cover}")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public Long addDish(@Valid C4Dish dish, @PathParam("cover") Long coverId,
            @HeaderParam("Token") String token) {
        return service.addDish(dish, coverId, token);
    }

    @POST
    @Path("/cookauth")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String processCookAuthRequest(@Valid C4AuthRequest req,
            @HeaderParam("Token") String token) {
        return service.processCookAuthRequest(req, token);
    }

    @POST
    @Path("/chat/from={from_id}&to={to_id}")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String sendChatMessage(@Valid String msg, @PathParam("from_id") Long fromId,
            @PathParam("to_id") Long toId, @HeaderParam("Token") String token) {
        return service.sendChatMessage(fromId, toId, msg, token);
    }

    @POST
    @Path("/offer")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String addItem(@Valid C4Item item, @HeaderParam("Token") String token) {
        return service.addOffer(item, token);
    }

    @POST
    @Path("/transaction")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String addTransaction(@Valid C4Transaction trans, @HeaderParam("Token") String token) {
        return service.addTransaction(trans, token);
    }

    @POST
    @Path("/temptransaction")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String addTempTransaction(@Valid C4Transaction trans, @HeaderParam("Token") String token) {
        return service.addTempTransaction(trans, token, true);
    }

    @GET
    @Path("/managepayments/id={id}&cmd={cmd}")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String managePayments(@Valid @PathParam("id") Long id, @Valid @PathParam("cmd") String cmd) {
        return service.managePayments(id, cmd);
    }

    @GET
    @Path("/paysuccess/id={id}")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String confirmOrDeleteTempTransaction(@Valid @PathParam("id") Long id) {
        return service.confirmOrDeleteTempTransaction(id, true);
    }

    @GET
    @Path("/payfail/id={id}")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String removeTempTransaction(@Valid @PathParam("id") Long id) {
        return service.removeTempTransaction(id);
    }

    @POST
    @Path("/proposeswap")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String addSwapProposal(@Valid C4SwapProposal swap, @HeaderParam("Token") String token) {
        return service.addSwapProposal(swap, token);
    }

    @POST
    @Path("/rating")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String addRating(@Valid C4Rating rating, @HeaderParam("Token") String token) {
        return service.addRating(rating, token);
    }

    @POST
    @Path("/image/dish={id}/cover={cover}")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String addImages(@Valid List<C4Image> images, @PathParam("id") Long id,
            @PathParam("cover") Long coverId, @HeaderParam("Token") String token) {
        return service.addImages(images, id, coverId, token);
    }

    @POST
    @Path("/userimage/user={id}")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public Long addUserImage(@Valid C4Image image, @PathParam("id") Long id,
            @HeaderParam("Token") String token) {
        return service.addUserImage(image, id, token);
    }

    @POST
    @Path("/imgdel")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String removeImages(String idsToRemoveString, @HeaderParam("Token") String token) {
        return service.removeImages(idsToRemoveString, token);
    }

    @DELETE
    @Path("/dish/{id}")
    @Produces({"text/plain"})
    public String removeDish(@PathParam("id") Long id, @HeaderParam("Token") String token) {
        return service.removeDish(id, token, true);
    }

    @DELETE
    @Path("/user/{id}")
    @Produces({"text/plain"})
    public String removeUser(@PathParam("id") Long id, @HeaderParam("Token") String token) {
        return service.removeUser(id, token);
    }

    @DELETE
    @Path("/transaction/id={id}&buy={buy}")
    @Produces({"text/plain"})
    public String removeTransaction(@PathParam("id") Long id,
            @PathParam("buy") boolean buy, @HeaderParam("Token") String token) {
        return service.removeTransaction(id, buy, token, false, null);
    }

    @DELETE
    @Path("/image/id={id}")
    @Produces({"text/plain"})
    public String removeImage(@PathParam("id") Long id,
            @HeaderParam("Token") String token) {
        return service.removeImage(id, token);
    }

    @DELETE
    @Path("/offer/{id}")
    @Produces({"text/plain"})
    public String removeOffer(@PathParam("id") Long id, @HeaderParam("Token") String token) {
        return service.removeOffer(id, token);
    }

    @GET
    @Produces({"application/json"})
    @Path("/dish/{id}")
    public List<C4Dish> getDishes(
            @PathParam("id")
            @NotNull Long id) {
        return service.getDishes(id);
    }

    @GET
    @Produces({"text/plain"})
    @Path("/swap/id={id}&force={force}")
    public String confirmSwap(@PathParam("id") @NotNull Long id,
            @PathParam("force") @NotNull Boolean force, @HeaderParam("Token") String token) {
        return service.confirmSwap(id, force, token);
    }

    @GET
    @Produces({"text/plain"})
    @Path("/setpayemail/id={id}&email={email}")
    public String setPayEmail(@PathParam("id") @NotNull Long id,
            @PathParam("email") @NotNull String email, @HeaderParam("Token") String token) {
        return service.setPayEmail(id, email, token);
    }

    @DELETE
    @Produces({"text/plain"})
    @Path("/swap/{id}")
    public String removeSwap(
            @PathParam("id")
            @NotNull Long id, @HeaderParam("Token") String token) {
        return service.removeSwap(id, token);
    }

    @GET
    @Produces({"application/json"})
    @Path("/getdish/{id}")
    public C4Dish getDish(
            @PathParam("id")
            @NotNull Long id) {
        return service.getDish(id);

    }

    @GET
    @Produces({"application/json"})
    @Path("/image/id={id}&small={small}")
    public C4Image getImage(
            @PathParam("id") @NotNull Long id, @PathParam("small") @NotNull Boolean small) {
        return service.getImage(id, small);
    }

    @GET
    @Produces({"image/jpeg"})
    @Path("/image-{id}")
    public Response getImageWeb(@PathParam("id") @NotNull Long id) {
        C4Image image = getImage(id, false);
        if (image == null) {
            throw new NotFoundException("Item not found");
        }
        ResponseBuilder rb = Response.ok(image.getBigImage());
        return rb.build();
    }

    @GET
    @Produces({"application/json"})
    @Path("/offer/{id}")
    public List<C4Item> getOffers(
            @PathParam("id")
            @NotNull Long id) {
        return service.getOffers(id);
    }

    @GET
    @Produces({"application/json"})
    @Path("/pendingswaps/{id}")
    public List<C4SwapProposal> getPendingSwaps(
            @PathParam("id")
            @NotNull Long id) {
        return service.getPendingSwaps(id);
    }

    @GET
    @Produces({"application/json"})
    @Path("/transaction/{id}")
    public List<C4Transaction> getTransactions(
            @PathParam("id")
            @NotNull Long id) {
        return service.getTransactions(id);
    }

    @GET
    @Produces({"application/json"})
    @Path("/user/id={id}&from={from_id}")
    public C4User getUser(
            @PathParam("id")
            @NotNull Long id, @PathParam("from_id")
            @NotNull Long fromId, @HeaderParam("Token") String token) {
        return service.getUser(id, fromId, token);
    }

    @GET
    @Produces({"application/json"})
    @Path("/usercomment/{id}")
    public List<C4UserComment> getUserComments(
            @PathParam("id")
            @NotNull Long id) {
        return service.getUserComments(id);
    }

    @GET
    @Produces({"application/json"})
    @Path("/dishcomment/{id}")
    public List<C4DishComment> getDishComments(
            @PathParam("id")
            @NotNull Long id) {
        return service.getDishComments(id);
    }

    @GET
    @Produces({"text/plain"})
    @Path("/changename/id={id}&name={name}")
    public String changeName(@PathParam("id") Long id,
            @NotNull @PathParam("name") String name, @HeaderParam("Token") String token) throws Exception {
        return service.changeName(id, name, token);
    }

    @POST
    @Produces({"text/plain"})
    @Consumes({"application/json"})
    @Path("/userdescription/id={id}")
    public String changeUserDescription(String desc, @PathParam("id") Long id,
            @HeaderParam("Token") String token) throws Exception {
        return service.changeUserDescription(id, desc, token);
    }

    @GET
    @Produces({"text/plain"})
    @Path("/test")
    public String test() throws Exception {
        return service.test();
    }

    @GET
    @Produces({"text/plain"})
    @Path("/sendnotification/id={id}&title={title}&text={text}")
    public String sendNotification(@PathParam("id") Long id, @PathParam("title") String title,
            @PathParam("text") String text) {
        return service.sendNotification(id, title, text);
    }

    @GET
    @Produces({"text/plain"})
    @Path("/deliverypoint")
    public String setDeliveryPoint(@QueryParam("id") Long id, @QueryParam("name") String name,
            @QueryParam("email") String email, @QueryParam("latitude") Double latitude,
            @QueryParam("longitude") Double longitude, @QueryParam("radius") Float radius,
            @QueryParam("price") Float price, @QueryParam("currency") String currency,
            @QueryParam("periods") String periods, @QueryParam("days") Integer days,
            @QueryParam("active") Integer active, @QueryParam("delete") Integer delete) {
        return service.setDeliveryPoint(id, name, email, latitude,
                longitude, radius, price, currency, periods, days, active, delete);
    }

    @GET
    @Produces({"text/plain"})
    @Path("/privilege")
    public String setPrivilege(@QueryParam("email") String email,
            @QueryParam("type") Integer type, @QueryParam("fiscal") String fiscal,
            @QueryParam("delete") Integer delete) {
        return service.setPrivilege(email, type, fiscal, delete);
    }

    @POST
    @Path("/clientlogs/{id}")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String registerLog(String message, @PathParam("id") Long id) {
        return service.registerLog(id, message);
    }

    @POST
    @Path("/report")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String registerReport(C4Report report, @HeaderParam("Token") String token) {
        return service.registerReport(report, token);
    }

    @GET
    @Path("/bandish/{id}")
    @Produces({"text/plain"})
    public String banDish(@PathParam("id") Long id) {
        return service.banDish(id, true);
    }

    @GET
    @Path("/banuser/{id}")
    @Produces({"text/plain"})
    public String banUser(@PathParam("id") Long id) {
        return service.banUser(id);
    }

    @GET
    @Path("/unbanuser/{id}")
    @Produces({"text/plain"})
    public String unBanUser(@PathParam("id") Long id) {
        return service.unbanUser(id);
    }

    @GET
    @Path("/removecomment/{id}")
    @Produces({"text/plain"})
    public String removeComment(@PathParam("id") Long id) {
        return service.removeComment(id);
    }

    @POST
    @Path("/signup/")
    @Consumes({"application/json"})
    @Produces({"text/plain"})
    public String signUp(@Valid C4SignUpRequest request) {
        return service.signUp(request);
    }

    @GET
    @Path("/confirm/id={id}&token={token}")
    @Produces({"text/plain"})
    @PermitAll
    public String confirm(@Context final HttpServletResponse response,
            @PathParam("id") Long id, @PathParam("token") String token) {
        String r = service.confirm(id, token);
        if (r.equals("OK")) {
            try {
                response.sendRedirect(Configs.WELCOME_PAGE);
            } catch (IOException ex) {
            }
        }
        return r;
    }

    @POST
    @Path("/query/{id}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public List<C4Item> queryItems(@Valid C4Query query, @PathParam("id") Long id) {
        return service.queryItems(query, id);
    }

    @GET
    @Produces({"application/json"})
    @Path("/tags")
    public List<C4Tag> getTags() {
        return service.getTags();
    }

    @GET
    @Produces({"application/json"})
    @Path("/tags/id={id}")
    public List<C4Tag> getTagsWithId(@PathParam("id") Long id) {
        return service.getTags(id);
    }

    @GET
    @Produces({"application/json"})
    @Path("/topcooks")
    public List<C4User> queryTopCooks() {
        return service.queryTopCooks();
    }

}
