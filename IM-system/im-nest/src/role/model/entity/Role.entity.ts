import { Entity, PrimaryGeneratedColumn, Column } from "typeorm"

@Entity()
export class Role {

    @PrimaryGeneratedColumn()
    id: number

    @Column()
    roleName: string

    @Column()
    roleKey: string
    
}
