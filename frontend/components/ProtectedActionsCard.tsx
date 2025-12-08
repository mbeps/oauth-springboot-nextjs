import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

type ProtectedActionsCardProps = {
  onAction: (action: string) => Promise<void>;
  loading: boolean;
};

export function ProtectedActionsCard({
  onAction,
  loading,
}: ProtectedActionsCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Test Protected Actions</CardTitle>
        <CardDescription>
          These buttons require authentication and will show error toasts
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="flex flex-wrap gap-3">
          <Button
            variant="outline"
            onClick={() => onAction("test_action")}
            disabled={loading}
          >
            Test Action
          </Button>
          <Button
            variant="outline"
            onClick={() => onAction("sample_operation")}
            disabled={loading}
          >
            Sample Operation
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
