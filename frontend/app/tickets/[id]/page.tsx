import { TicketDetailsView } from "@/components/tickets/TicketDetailsView";

/**
 * DEC-009a: `/tickets/[id]` ticket details (UIF-003).
 * Edit / comments / transitions are added in later steps.
 */
type TicketDetailsPageProps = {
  params: Promise<{ id: string }>;
};

export default async function TicketDetailsPage({ params }: TicketDetailsPageProps) {
  const { id } = await params;
  return <TicketDetailsView ticketId={id} />;
}
