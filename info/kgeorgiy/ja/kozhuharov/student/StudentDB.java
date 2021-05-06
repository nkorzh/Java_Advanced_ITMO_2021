package info.kgeorgiy.ja.kozhuharov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.GroupQuery;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Student Database queries class
 *
 * @author Kozhukharov Nikita
 */
public class StudentDB implements GroupQuery {

    private static final Comparator<Student> nameComparator =
            Comparator.comparing(Student::getLastName).reversed()
                    .thenComparing(Comparator.comparing(Student::getFirstName).reversed())
                    .thenComparing(Comparator.naturalOrder());


    private <T> Stream<T> mapToStream(List<Student> students, Function<Student, T> getAttribute) {
        return students.stream().map(getAttribute);
    }

    private <T> List<T> mapToList(List<Student> students, Function<Student, T> getAttribute) {
        return mapToStream(students, getAttribute).collect(Collectors.toList());
    }

    /**
     * Get {@link Student Student} attribute
     */
    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapToList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapToList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return mapToList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapToList(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return mapToStream(students, Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }
    
    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Comparator.naturalOrder())
                .map(Student::getFirstName)
                .orElse("");
    }

    /**
     * Sorting
     */
    private Stream<Student> sortStudents(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream().sorted(comparator);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudents(students, Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudents(students, nameComparator)
                .collect(Collectors.toList());
    }

    /**
     * Find by methods
     */
    private <T> List<Student> filterStudentsBy(Collection<Student> students,
                                               final Function<Student, T> getField,
                                               final T fieldValue) {
        return students.stream()
                .filter(student -> getField.apply(student).equals(fieldValue))
                .sorted(nameComparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return filterStudentsBy(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return filterStudentsBy(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return filterStudentsBy(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group).stream()
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        (name1, name2) -> name1.compareTo(name2) <= 0 ? name1 : name2
                ));
    }

    /**
     * Group queries
     */
    private Stream<Group> mapInGroups(Collection<Student> students,
                                      final UnaryOperator<List<Student>> studentListProcessor) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup))
                .entrySet()
                .stream()
                .map(groupListPair ->
                        new Group(
                                groupListPair.getKey(),
                                studentListProcessor.apply(groupListPair.getValue())));
    }

    private List<Group> getListGroupsBy(Collection<Student> students,
                                        final UnaryOperator<List<Student>> studentListProcessor) {
        return mapInGroups(students, studentListProcessor)
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getListGroupsBy(students, this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getListGroupsBy(students, this::sortStudentsById);
    }

    private GroupName getLargestGroupNameBy(Collection<Student> students,
                                            final Function<List<Student>, Integer> listStatGetter,
                                            final Comparator<Group> groupNameComparator) {
        return mapInGroups(students, UnaryOperator.identity())
                .collect(Collectors.toMap(
                        UnaryOperator.identity(),
                        entry -> listStatGetter.apply(entry.getStudents())))
                .entrySet()
                .stream()
                .max(Map.Entry.<Group, Integer>comparingByValue(Comparator.naturalOrder())
                        .thenComparing(Map.Entry.comparingByKey(groupNameComparator)))
                .map(Map.Entry::getKey)
                .map(Group::getName)
                .orElse(null);
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getLargestGroupNameBy(
                students,
                List::size,
                Comparator.comparing(Group::getName));
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupNameBy(
                students,
                listOfStudents -> getDistinctFirstNames(listOfStudents).size(),
                Comparator.comparing(Group::getName).reversed());
    }
}
