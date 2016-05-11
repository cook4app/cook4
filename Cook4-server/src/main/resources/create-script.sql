CREATE TABLE app.dashboard_users (username VARCHAR(20) NOT NULL, password VARCHAR(255) NOT NULL, PRIMARY KEY (username))
CREATE TABLE app.dashboard_groups (username VARCHAR(20) NOT NULL, group_name VARCHAR(20) NOT NULL, PRIMARY KEY (username, group_name))
ALTER TABLE app.dashboard_groups ADD CONSTRAINT fk_dashusername FOREIGN KEY(username) REFERENCES app.dashboard_users (username)