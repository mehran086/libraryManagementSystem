package library.repository;

import library.domain.User;

public class UserRepository extends AbstractRepository<User,String>{

    @Override
    protected String getId(User item) {
        return item.getId();
    }


}
