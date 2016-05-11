<%@page import="com.beppeben.cook4server.utils.Configs"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
<%@ page import="java.sql.*, java.text.SimpleDateFormat,
         org.joda.time.*, javax.sql.*, java.io.*, javax.naming.*" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Cook4 Dashboard</title>
        <style type="text/css">
            html, body, #map-canvas { height: 100%; margin: 0; padding: 0;}
            #legend {
                padding: 10px;            
                width: 400px;
                height:200px;
                border: 2px solid gray;
                margin: 0;
                background: #FFFFFF;
                font-size: 150%;
            }

            p { margin:5px;}

        </style>
        <script type="text/javascript"
                src="https://maps.googleapis.com/maps/api/js?key=<%=Configs.MAPS_KEY%>">
        </script>

        <script type="text/javascript">
            var users = [];
            var stats = [];

            <%
                InitialContext ctx;
                DataSource ds;
                Connection conn = null;
                Statement stmt;
                ResultSet rs = null;
                DateTime today = new DateTime().withZone(DateTimeZone.forID("Europe/Rome"));

                try {
                    ctx = new InitialContext();
                    ds = (DataSource) ctx.lookup("jdbc/C4Data");
                    conn = ds.getConnection();
                    stmt = conn.createStatement();
                    Set<String> newUsers = new HashSet<String>();
                    int maxlag = 3;
                    for (int lag = 0; lag <= maxlag; lag++) {
                        DateTime date = today.minusDays(lag);
                        String key = date.getDayOfMonth() + "-" + date.getMonthOfYear() + "-" + date.getYear();
                        rs = stmt.executeQuery("SELECT * FROM app.cook4_statistics where date='" + key + "'");
                        if (rs.next()) {
            %>
            stats[stats.length] = [
                "<%=rs.getString("Date")%>",
                "<%=rs.getString("Uniqueusers")%>",
                "<%=rs.getString("Totalsearches")%>",
                "<%=rs.getString("Transactions")%>",
                "<%=rs.getString("Chats")%>"];
            <%
                        if (lag == 0) {
                            String r = rs.getString("NewIds");
                            if (r != null) {
                                String[] ids = r.split("-");
                                for (String id : ids) {
                                    newUsers.add(id);
                                }
                            }
                        }
                    }
                }

                rs = stmt.executeQuery("SELECT app.cook4_user.id, app.cook4_user.name, app.cook4_user.lastactive, app.cook4_user.latitude, app.cook4_user.longitude, app.cook4_user.email, app.cook4_user.sellexperience, app.cook4_user.generalexperience, count(distinct app.cook4_dish.id) as numdishes, count(distinct app.cook4_item.id) as numoffers "
                        + "FROM app.cook4_user "
                        + "LEFT OUTER JOIN app.cook4_dish on app.cook4_user.id=app.cook4_dish.user_id "
                        + "LEFT OUTER JOIN app.cook4_item on app.cook4_dish.id=app.cook4_item.dish_id "
                        + "GROUP BY app.cook4_user.id, app.cook4_user.name, app.cook4_user.lastactive, app.cook4_user.latitude, app.cook4_user.longitude, app.cook4_user.email, app.cook4_user.sellexperience, app.cook4_user.generalexperience");

                while (rs.next()) {
                    if (rs.getString("Latitude") == null) {
                        continue;
                    }
                    DateTime date = new DateTime(rs.getTimestamp("Lastactive"));
                    int days = Days.daysBetween(date, today).getDays();
                    double weeks = ((double) (days + 1)) / 7.0;
                    double opacity = Math.pow(0.93, days);

                    String dateString = new SimpleDateFormat("dd-MM-yyyy HH:mm")
                            .format(date.plusHours(6).toDate());

            %>
            users[users.length] = [
                "<%=rs.getString("Name")%>",
            <%=rs.getString("Latitude")%>,
            <%=rs.getString("Longitude")%>,
                "<%=rs.getString("Email")%>",
                "<%=rs.getInt("Sellexperience")%>",
                "<%=rs.getInt("Generalexperience") - rs.getInt("Sellexperience")%>",
                "<%=dateString%>",
                "<%=rs.getString("Id")%>",
            <%=opacity%>,
                "<%=rs.getString("Numdishes")%>",
                "<%=rs.getString("Numoffers")%>",
            <%=newUsers.contains(rs.getString("Id"))%>];
            <%
                    }

                    stmt.close();
                    conn.close();
                    conn.commit();
                } catch (SQLException se) {
                } catch (NamingException ne) {
                }
            %>

        </script>

        <script type="text/javascript">

            function initialize() {
                var mapOptions = {
                    center: {lat: 45, lng: 9},
                    zoom: 5
                };
                var map = new google.maps.Map(document.getElementById('map-canvas'),
                        mapOptions);

                map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(
                        document.getElementById('legend'));

                var infowindow = new google.maps.InfoWindow();
                var marker, i;
                var iconbase = 'http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|';

                for (i = 0; i < users.length; i++) {
                    marker = new google.maps.Marker({
                        position: new google.maps.LatLng(users[i][1], users[i][2]),
                        map: map,
                        opacity: users[i][8]
                    });
                    if (users[i][11]) {
                        marker.setIcon('http://maps.google.com/mapfiles/ms/icons/green-dot.png');
                    }
                    google.maps.event.addListener(marker, 'click', (function(marker, i) {
                        return function() {
                            var content = '<div id="content">' +
                                    '<h1 id="firstHeading" class="firstHeading">' + users[i][0] + '</h1>' +
                                    '<div id="bodyContent">' +
                                    '<p><b>Email: </b>' + users[i][3] + '</p>' +
                                    '<p><b>Sell Experience: </b>' + users[i][4] + '</p>' +
                                    '<p><b>Buy Experience: </b>' + users[i][5] + '</p>' +
                                    '<p><b>Registered dishes: </b>' + users[i][9] + '</p>' +
                                    '<p><b>Outstanding offers: </b>' + users[i][10] + '</p>' +
                                    '<p><b>Last Active: </b>' + users[i][6] + '</p>' +
                                    '<p><b>Id: </b>' + users[i][7] + '</p>' +
                                    '</div>' +
                                    '</div>';
                            infowindow.setContent(content);
                            infowindow.open(map, marker);
                        }
                    })(marker, i));

                    google.maps.event.addListener(map, "click", function(event) {
                        infowindow.close();
                    });
                }

                var legend = "<h2>Cook4 Dashboard</h2>" +
                        "<p>Registered users: " + users.length + "</p>";
                for (var i = 0; i < stats.length; i++) {
                    legend += "<p>" + stats[i][0] + " U: " + stats[i][1] +
                            " S: " + stats[i][2] + " T: " + stats[i][3] +
                            " C: " + stats[i][4] + " </p>";
                }

                document.getElementById("legend").innerHTML = legend;
            }

            google.maps.event.addDomListener(window, 'load', initialize);
        </script>
    </head>

    <body>
        <div id="map-canvas"></div>
        <div id="legend"></div>
    </body>

</html>

