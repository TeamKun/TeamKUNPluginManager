package net.kunmc.lab.teamkunpluginmanager.kpminfo;

public class InvalidInformationFileException extends Exception
{
    public InvalidInformationFileException(String message)
    {
        super(message);
    }

    public InvalidInformationFileException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
