package com.example.timetable.service.impl;


import com.example.timetable.entity.Instructor;
import com.example.timetable.entity.User;
import com.example.timetable.exception.UserAlreadyExistsException;
import com.example.timetable.repository.InstructorRepository;
import com.example.timetable.repository.UserRepository;
import com.example.timetable.service.InstructorService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;

    @Override
    public List<Instructor> findAll() {
        return instructorRepository.findAll();
    }

    @Override
    public Instructor findById(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Instructor not found with id: " + id
                        )
                );
    }

    @Override
    public Instructor findByEmail(String email) {
        return instructorRepository
                .findByUserEmail(email)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Instructor not found with email: " + email
                        )
                );
    }

    @Override
    public Instructor save(Instructor instructor) {
        User user = instructor.getUser();
        String email = user.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already registered: " + email);
        }
        userRepository.save(user);
        return instructorRepository.save(instructor);
    }

    @Override
    public void deleteById(Long id) {
        if (!instructorRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Instructor not found with id: " + id
            );
        }
        instructorRepository.deleteById(id);
    }
}