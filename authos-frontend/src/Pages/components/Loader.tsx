import {HashLoader} from "react-spinners";

export const Loader = () => {
    return (
        <div className="fixed inset-0 flex items-center justify-center z-50">
            <HashLoader loading={true} color="#02ab79"/>
        </div>
    )

}