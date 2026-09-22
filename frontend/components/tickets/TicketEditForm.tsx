"use client";

import { useId, useMemo, useRef, useState, type FormEvent } from "react";
import { InlineAlert } from "@/components/ui/InlineAlert";
import { ApiClientError } from "@/lib/api/http";
import { updateTicket } from "@/lib/api/tickets";
import { ErrorCodes, PRIORITIES, type ErrorDetail, type Priority, type TicketDetailDto, type UpdateTicketRequest } from "@/lib/types/api";

type FieldErrors = Partial<Record<"title" | "description" | "priority" | "assignee", string>>;

const TITLE_MAX = 200;
const DESCRIPTION_MAX = 10_000;
const ASSIGNEE_MAX = 120;

type TicketEditFormProps = {
  ticket: TicketDetailDto;
  onCancel: () => void;
  onSaved: (updated: TicketDetailDto) => void;
  onNotFound: (message: string) => void;
};

/**
 * Inline edit form on the details page (UIF-004 / DEC-009c).
 * Status is never collected or sent.
 */
export function TicketEditForm({ ticket, onCancel, onSaved, onNotFound }: TicketEditFormProps) {
  const formId = useId();
  const titleRef = useRef<HTMLInputElement>(null);

  const [title, setTitle] = useState(ticket.title);
  const [description, setDescription] = useState(ticket.description ?? "");
  const [priority, setPriority] = useState<Priority>(ticket.priority);
  const [assignee, setAssignee] = useState(ticket.assignee ?? "");

  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<{ message: string; details?: ErrorDetail[] } | null>(
    null
  );
  const [submitting, setSubmitting] = useState(false);

  const patchBody = useMemo(() => buildPatchBody(ticket, title, description, priority, assignee), [
    ticket,
    title,
    description,
    priority,
    assignee,
  ]);

  function validateClient(): FieldErrors {
    const errors: FieldErrors = {};
    const trimmedTitle = title.trim();

    if (!trimmedTitle) {
      errors.title = "Title is required";
    } else if (trimmedTitle.length > TITLE_MAX) {
      errors.title = `Title must be at most ${TITLE_MAX} characters`;
    }

    if (description.length > DESCRIPTION_MAX) {
      errors.description = `Description must be at most ${DESCRIPTION_MAX} characters`;
    }

    if (assignee.trim().length > ASSIGNEE_MAX) {
      errors.assignee = `Assignee must be at most ${ASSIGNEE_MAX} characters`;
    }

    if (!PRIORITIES.includes(priority)) {
      errors.priority = "Priority must be LOW, MEDIUM, or HIGH";
    }

    return errors;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (submitting) {
      return;
    }

    const clientErrors = validateClient();
    setFieldErrors(clientErrors);
    setFormError(null);

    if (Object.keys(clientErrors).length > 0) {
      titleRef.current?.focus();
      return;
    }

    if (Object.keys(patchBody).length === 0) {
      setFormError({ message: "Change at least one field before saving." });
      return;
    }

    if ("status" in patchBody) {
      setFormError({ message: "Status cannot be changed through edit." });
      return;
    }

    setSubmitting(true);
    try {
      const updated = await updateTicket(ticket.id, patchBody);
      onSaved(updated);
    } catch (err) {
      if (err instanceof ApiClientError) {
        if (err.status === 404 || err.errorDto?.code === ErrorCodes.TICKET_NOT_FOUND) {
          onNotFound(err.errorDto?.message ?? "This ticket could not be found.");
          return;
        }
        const details = err.errorDto?.details ?? [];
        const nextFieldErrors: FieldErrors = {};
        for (const detail of details) {
          const field = detail.field;
          if (
            field === "title" ||
            field === "description" ||
            field === "priority" ||
            field === "assignee"
          ) {
            nextFieldErrors[field] = detail.message;
          }
        }
        setFieldErrors(nextFieldErrors);
        setFormError({
          message: err.message,
          details: details.filter(
            (detail) =>
              detail.field !== "title" &&
              detail.field !== "description" &&
              detail.field !== "priority" &&
              detail.field !== "assignee"
          ),
        });
      } else {
        setFormError({ message: "Unable to save changes. Please try again." });
      }
      setSubmitting(false);
    }
  }

  return (
    <section className="ticket-panel ticket-edit" aria-labelledby={`${formId}-heading`}>
      <h2 id={`${formId}-heading`}>Edit ticket</h2>
      <p className="ticket-edit__hint">
        Status stays <strong>{ticket.status.replaceAll("_", " ")}</strong> here. Use a status
        transition action later to change it.
      </p>

      {formError ? (
        <InlineAlert
          tone="error"
          title="Could not save changes"
          message={formError.message}
          details={formError.details}
        />
      ) : null}

      <form className="ticket-form" onSubmit={handleSubmit} noValidate aria-busy={submitting}>
        <div className="field">
          <label className="field__label" htmlFor={`${formId}-title`}>
            Title <span className="field__required">(required)</span>
          </label>
          <input
            ref={titleRef}
            id={`${formId}-title`}
            name="title"
            type="text"
            value={title}
            maxLength={TITLE_MAX}
            onChange={(event) => setTitle(event.target.value)}
            aria-invalid={Boolean(fieldErrors.title)}
            aria-describedby={fieldErrors.title ? `${formId}-title-error` : undefined}
            disabled={submitting}
            autoComplete="off"
          />
          {fieldErrors.title ? (
            <p id={`${formId}-title-error`} className="field__error" role="alert">
              {fieldErrors.title}
            </p>
          ) : null}
        </div>

        <div className="field">
          <label className="field__label" htmlFor={`${formId}-description`}>
            Description
          </label>
          <textarea
            id={`${formId}-description`}
            name="description"
            rows={5}
            value={description}
            maxLength={DESCRIPTION_MAX}
            onChange={(event) => setDescription(event.target.value)}
            aria-invalid={Boolean(fieldErrors.description)}
            aria-describedby={fieldErrors.description ? `${formId}-description-error` : undefined}
            disabled={submitting}
          />
          {fieldErrors.description ? (
            <p id={`${formId}-description-error`} className="field__error" role="alert">
              {fieldErrors.description}
            </p>
          ) : null}
        </div>

        <div className="ticket-form__row">
          <div className="field">
            <label className="field__label" htmlFor={`${formId}-priority`}>
              Priority
            </label>
            <select
              id={`${formId}-priority`}
              name="priority"
              value={priority}
              onChange={(event) => setPriority(event.target.value as Priority)}
              aria-invalid={Boolean(fieldErrors.priority)}
              aria-describedby={fieldErrors.priority ? `${formId}-priority-error` : undefined}
              disabled={submitting}
            >
              {PRIORITIES.map((value) => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </select>
            {fieldErrors.priority ? (
              <p id={`${formId}-priority-error`} className="field__error" role="alert">
                {fieldErrors.priority}
              </p>
            ) : null}
          </div>

          <div className="field">
            <label className="field__label" htmlFor={`${formId}-assignee`}>
              Assignee
            </label>
            <input
              id={`${formId}-assignee`}
              name="assignee"
              type="text"
              value={assignee}
              maxLength={ASSIGNEE_MAX}
              onChange={(event) => setAssignee(event.target.value)}
              aria-invalid={Boolean(fieldErrors.assignee)}
              aria-describedby={fieldErrors.assignee ? `${formId}-assignee-error` : undefined}
              disabled={submitting}
              autoComplete="off"
              placeholder="Leave blank to unassign"
            />
            {fieldErrors.assignee ? (
              <p id={`${formId}-assignee-error`} className="field__error" role="alert">
                {fieldErrors.assignee}
              </p>
            ) : null}
          </div>
        </div>

        <div className="ticket-form__actions">
          <button type="submit" className="button button--primary" disabled={submitting}>
            {submitting ? "Saving…" : "Save changes"}
          </button>
          <button
            type="button"
            className="button button--secondary"
            onClick={onCancel}
            disabled={submitting}
          >
            Cancel
          </button>
        </div>
      </form>
    </section>
  );
}

function buildPatchBody(
  ticket: TicketDetailDto,
  title: string,
  description: string,
  priority: Priority,
  assignee: string
): UpdateTicketRequest {
  const body: UpdateTicketRequest = {};
  const nextTitle = title.trim();
  const nextDescription = description;
  const nextAssignee = assignee.trim();
  const currentDescription = ticket.description ?? "";
  const currentAssignee = ticket.assignee ?? "";

  if (nextTitle !== ticket.title) {
    body.title = nextTitle;
  }
  if (nextDescription !== currentDescription) {
    body.description = nextDescription.trim() === "" ? null : nextDescription;
  }
  if (priority !== ticket.priority) {
    body.priority = priority;
  }
  if (nextAssignee !== currentAssignee) {
    body.assignee = nextAssignee === "" ? "" : nextAssignee;
  }

  return body;
}
