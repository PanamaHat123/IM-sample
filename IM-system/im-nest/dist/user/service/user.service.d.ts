import { User } from '../entity/User.entity';
export declare class UserService {
    private manager;
    getUser(): Promise<User>;
}
