# Adding a new CRUD feature

`folder/` is the reference implementation. For a new entity (say `Note`), create a `note/` package next to it
and copy the pattern file by file.

## 1. Database: `src/main/resources/db/migration/V<next>__create_note.sql`
- [ ] New migration file with the next version number. **Never edit a migration that has already run.**
- [ ] Column types and lengths match the entity and the request validation (`VARCHAR(50)` ↔ `@Size(max = 50)`).
- [ ] Foreign keys for every reference to another table.
- [ ] `created_at` / `updated_at` columns.
- [ ] If the table already has data: will this migration still work on it? (See `V2`'s orphan cleanup.)

## 2. Entity: `note/Note.java`
- [ ] `@Getter` only, no `@Setter` / `@Data`.
- [ ] `@NoArgsConstructor(access = AccessLevel.PROTECTED)` for JPA, plus a public constructor with the required fields.
- [ ] Change state through named methods (`rename(...)`, `moveTo(...)`), not setters.
- [ ] Relationships: `@ManyToOne(fetch = FetchType.LAZY)`.
- [ ] `@CreationTimestamp` / `@UpdateTimestamp`.

## 3. Repository: `note/NoteRepository.java`
- [ ] Package-private `interface NoteRepository extends JpaRepository<Note, Long>`.
- [ ] Only add query methods you actually use.

## 4. DTOs: `note/dto/`
- [ ] `CreateNoteRequest`, `UpdateNoteRequest`, `NoteResponse` as **records**.
- [ ] Validation annotations on every request field (`@NotBlank`, `@Size`, `@Positive`, ...).
- [ ] `NoteResponse.from(Note)`: the entity never leaves the service.

## 5. Exception: `note/NoteNotFoundException.java`
- [ ] `extends NotFoundException` → automatically becomes a 404. Business-rule violations: `throw new ConflictException(...)` → 409.

## 6. Service: `note/NoteService.java`
- [ ] `@Transactional(readOnly = true)` on the class, `@Transactional` on write methods.
- [ ] Takes request DTOs, returns response DTOs.
- [ ] All business rules live here (existence checks, "cannot delete when ...", ...).

## 7. Controller: `note/NoteController.java`
- [ ] `@RequestMapping("/api/notes")`: plural noun.
- [ ] `@Valid @RequestBody` on every body.
- [ ] POST → `201 Created` + `Location`; GET → 200; PUT → 200; DELETE → `204 No Content`.
- [ ] Lists are paginated: `@PageableDefault` + `PagedModel`.
- [ ] No logic: one call to the service per method.

## 8. Tests
- [ ] `NoteServiceTest`: Mockito unit tests, one per business rule (happy path + each failure).
- [ ] `NoteControllerTest`: `@WebMvcTest`, checks status codes, `Location`, validation errors, error JSON.
- [ ] `NoteIntegrationTest`: `@SpringBootTest` + Testcontainers, one full lifecycle + the important rules.
- [ ] `./mvnw clean verify` is green before you commit.
