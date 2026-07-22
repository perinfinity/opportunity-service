db = db.getSiblingDB("opportunities");

db.createUser({
  user: "admin",
  pwd: "password",
  roles: [{ role: "readWrite", db: "opportunities" }]
});