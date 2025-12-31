import sys
import subprocess

def main():
    if len(sys.argv) != 3:
        print("Usage: delphix_synch.py <snapshotvdb> <snapshotid>")
        sys.exit(1)

    snapshotvdb = sys.argv[1]
    snapshotid = sys.argv[2]

    # Command to restore snapshot
    command = f"/var/lib/jenkins/dct-toolkit refresh_vdb_by_snapshot vdb_id={snapshotvdb} snapshot_id={snapshotid} -js"

    print(f"🚀 Starting snapshot restore for VDB: {snapshotvdb}, Snapshot: {snapshotid}")
    print(f"🔗 Running command: {command}\n")

    try:
        # Run the command and capture output
        result = subprocess.run(
            command,
            shell=True,
            capture_output=True,
            text=True
        )

        # Print stdout
        if result.stdout:
            print("🧪 Command output:")
            for line in result.stdout.splitlines():
                print(f"    {line}")

        # Print stderr
        if result.stderr:
            print("⚠️ Command errors:")
            for line in result.stderr.splitlines():
                print(f"    {line}")

        # Check return code
        if result.returncode == 0:
            print("\n✅ Snapshot restore completed successfully")
        else:
            print("\n❌ Snapshot restore failed")
            sys.exit(result.returncode)

    except Exception as e:
        print(f"❌ Exception while running command: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()