import '../../App.css';
import React, {useEffect, useState} from "react";
import {getUsers} from "../../comunication/FetchUser";

/**
 * Users
 * @author Peter Rutschmann
 */
const Users = ({loginValues}) => {
    const [users, setUsers] = useState([]);
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const users = await getUsers();
                console.log(users);
                setUsers(users);
            } catch (error) {
                console.error('Failed to fetch to server:', error.message);
                setErrorMessage(error.message);
            }
        };
        fetchUsers().then();
    }, [loginValues]);

    return (<>
            <h1>Client list</h1>
            <ul>
                {users.map(user => (
                    <li key={user.userUuid}>{user.userUuid} {user.firstName} {user.lastName} - {user.email}</li>))}
            </ul>
            {errorMessage && <p style={{color: 'red'}}>{errorMessage}</p>}
        </>);
};

export default Users;
