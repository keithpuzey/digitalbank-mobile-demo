import sys
import subprocess


def main():
    if len(sys.argv) != 3:
        print("Usage: delphix_synch.py <snapshotvdb> <snapshotid>")
        sys.exit(1)

    snapshotvdb = sys.argv[1]
    snapshotid = sys.argv[2]

    # Navigate to the directory
    subprocess.run(["cd", "/var/lib/jenkins"], shell=True)

    # Execute the command
    command = f"./dct-toolkit refresh_vdb_by_snapshot vdb_id={snapshotvdb} snapshot_id={snapshotid} -js"
    subprocess.run(command, shell=True)


if __name__ == "__main__":
    main()
