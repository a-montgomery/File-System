import java.util.Vector;

public class FileTable {

    private Vector table;         // the actual entity of this file table
    private Directory dir;        // the root directory

    public FileTable( Directory directory ) { // constructor
        table = new Vector( );     // instantiate a file (structure) table
        dir = directory;           // receive a reference to the Director
    }                             // from the file system

    // major public methods
    public synchronized FileTableEntry falloc( String filename, String mode ) {
        // allocate a new file (structure) table entry for this file name
        // allocate/retrieve and register the corresponding inode using dir



        short iNumber = filename.equals("/") ? 0 : dir.namei(filename);
        Inode inode;

        while(true){

            if(iNumber > 0){
                inode = new Inode(iNumber);

                if(mode.equals("r")){
                    inode.flag = 3; //read
                    break;
                }
                else{
                    inode.flag = 4; //write
                    break;
                }

            }

            if(!mode.equals("r")){
                iNumber = dir.ialloc(filename);
                inode = new Inode(iNumber);
                inode.flag = 3; //read
                break;

            }
            return null;

        }
        // increment this inode's count
        inode.count++;
        // immediately write back this inode to the disk
        inode.toDisk(iNumber);
        // return a reference to this file (structure) table entry
        FileTableEntry entry = new FileTableEntry(inode, iNumber, mode);
        table.addElement(entry);
        return entry;

    }

    public synchronized boolean ffree( FileTableEntry entry ) {
        Inode inode = new Inode(entry.iNumber);
        if (table.remove(entry)) {
            if (inode.flag == 2) {
                if (inode.count == 1) {
                    notify();
                    inode.flag = 1;
                }
            }
            else if (inode.flag == 3) {
                inode.flag = 1;
                notifyAll();
            }
            inode.count--;
            inode.toDisk(entry.iNumber);
            return true;
        }
        return false;
    }

    public synchronized boolean fempty( ) {
        return table.isEmpty( );  // return if table is empty
    }                            // should be called before starting a format
}
